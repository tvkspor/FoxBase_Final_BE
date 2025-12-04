package com.be.java.foxbase.service;

import com.be.java.foxbase.configuration.ZaloPayConfig;
import com.be.java.foxbase.db.entity.Order;
import com.be.java.foxbase.db.entity.PaymentTransaction;
import com.be.java.foxbase.db.entity.PurchasedBook;
import com.be.java.foxbase.db.key.UserBookId;
import com.be.java.foxbase.dto.request.ZaloPayWebhookRequest;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.repository.OrderRepository;
import com.be.java.foxbase.repository.PaymentTransactionRepository;
import com.be.java.foxbase.repository.PurchasedBookRepository;
import com.be.java.foxbase.utils.OrderStatus;
import com.be.java.foxbase.vn.zalopay.crypto.HMACUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ZaloPayWebhookService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private PurchasedBookRepository purchasedBookRepository;
    
    @Autowired
    private ZaloPayConfig zaloPayConfig;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public boolean verifyMac(ZaloPayWebhookRequest request) {
        try {
            List<String> macData = Arrays.asList(
                zaloPayConfig.getKey2() != null ? zaloPayConfig.getKey2() : "",
                request.getAppId() != null ? request.getAppId() : String.valueOf(zaloPayConfig.getAppIdInt()),
                request.getAppTransId() != null ? request.getAppTransId() : "",
                request.getPmcId() != null ? request.getPmcId() : "",
                request.getBankCode() != null ? request.getBankCode() : "",
                request.getAmount() != null ? String.valueOf(request.getAmount()) : "0",
                request.getDiscountAmount() != null ? String.valueOf(request.getDiscountAmount()) : "0",
                request.getStatus() != null ? String.valueOf(request.getStatus()) : "0",
                request.getZpTransId() != null ? request.getZpTransId() : "",
                request.getServerDate() != null ? String.valueOf(request.getServerDate()) : "0"
            );
            
            String macInput = String.join("|", macData);
            String calculatedMac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zaloPayConfig.getKey2(), macInput);
            
            boolean isValid = calculatedMac != null && calculatedMac.equalsIgnoreCase(request.getChecksum());
            
            log.info("Webhook MAC verification for appTransId={}: isValid={}", request.getAppTransId(), isValid);
            
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying MAC for appTransId={}: {}", request.getAppTransId(), e.getMessage(), e);
            return false;
        }
    }
    
    @Transactional
    public void processWebhook(ZaloPayWebhookRequest request, String ipAddress) {
        String appTransId = request.getAppTransId();
        log.info("Processing webhook for appTransId={}, ipAddress={}", appTransId, ipAddress);
        PaymentTransaction transaction = PaymentTransaction.builder()
            .appTransId(appTransId)
            .source("webhook")
            .ipAddress(ipAddress)
            .requestData(toJson(request))
            .returnCode(request.getStatus())
            .returnMessage("Webhook received")
            .zpTransId(request.getZpTransId())
            .macValid(false)
            .build();
        boolean macValid = verifyMac(request);
        transaction.setMacValid(macValid);
        if (!macValid) {
            transaction.setErrorMessage("Invalid MAC checksum");
            paymentTransactionRepository.save(transaction);
            log.error("Webhook rejected: Invalid MAC for appTransId={}", appTransId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Order order = orderRepository.findByAppTransId(appTransId)
            .orElse(null);
        
        if (order == null) {
            transaction.setErrorMessage("Order not found");
            paymentTransactionRepository.save(transaction);
            log.warn("Webhook received for non-existent order: appTransId={}", appTransId);
            return;
        }
        if (order.getStatus() == OrderStatus.PAID && order.getWebhookReceived()) {
            log.info("Webhook already processed for appTransId={}, status=PAID. Skipping.", appTransId);
            transaction.setResponseData("Already processed - order is PAID");
            paymentTransactionRepository.save(transaction);
            return;
        }
        
        OrderStatus newStatus = mapZaloPayStatus(request.getStatus());
        order.setZpTransId(request.getZpTransId());
        order.setWebhookReceived(true);
        
        if (newStatus == OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            updatePurchasedBook(order);
        } else if (newStatus == OrderStatus.FAILED) {
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Payment failed - return code: " + request.getStatus());
        } else if (newStatus == OrderStatus.EXPIRED) {
            order.setStatus(OrderStatus.EXPIRED);
            order.setExpiredAt(LocalDateTime.now());
        }
        
        orderRepository.save(order);
        transaction.setResponseData("Order status updated to: " + newStatus);
        paymentTransactionRepository.save(transaction);
        
        log.info("Webhook processed successfully for appTransId={}, newStatus={}", appTransId, newStatus);
    }
    
    private OrderStatus mapZaloPayStatus(Integer status) {
        if (status == null) {
            return OrderStatus.PENDING;
        }
        
        switch (status) {
            case 1:
                return OrderStatus.PAID;
            case 2:
                return OrderStatus.FAILED;
            case -51:
                return OrderStatus.EXPIRED;
            default:
                log.warn("Unknown ZaloPay status code: {}", status);
                return OrderStatus.FAILED;
        }
    }
    
    private void updatePurchasedBook(Order order) {
        try {
            UserBookId userBookId = new UserBookId(order.getUser().getUsername(), order.getBook().getBookId());
            
            PurchasedBook purchasedBook = purchasedBookRepository.findById(userBookId)
                .orElse(PurchasedBook.builder()
                    .id(userBookId)
                    .user(order.getUser())
                    .book(order.getBook())
                    .paid(false)
                    .createdAt(order.getCreatedAt())
                    .paidAt(null)
                    .build());
            
            purchasedBook.setPaid(true);
            purchasedBook.setPaidAt(order.getPaidAt() != null ? order.getPaidAt() : LocalDateTime.now());
            
            purchasedBookRepository.save(purchasedBook);
            
            log.info("Updated PurchasedBook for appTransId={}, username={}, bookId={}", 
                order.getAppTransId(), order.getUser().getUsername(), order.getBook().getBookId());
        } catch (Exception e) {
            log.error("Error updating PurchasedBook for appTransId={}: {}", order.getAppTransId(), e.getMessage(), e);
        }
    }
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}

