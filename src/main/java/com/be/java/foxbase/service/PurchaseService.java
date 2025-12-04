package com.be.java.foxbase.service;

import com.be.java.foxbase.configuration.ZaloPayConfig;
import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.Order;
import com.be.java.foxbase.db.entity.PaymentTransaction;
import com.be.java.foxbase.db.entity.PurchasedBook;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookId;
import com.be.java.foxbase.dto.request.PurchaseBookRequest;
import com.be.java.foxbase.dto.request.PurchaseWalletRequest;
import com.be.java.foxbase.dto.request.ZaloPayOrderRequest;
import com.be.java.foxbase.dto.response.CreateOrderResponse;
import com.be.java.foxbase.dto.response.OrderStatusResponse;
import com.be.java.foxbase.dto.response.PurchaseBookResponse;
import com.be.java.foxbase.dto.response.PurchaseWalletResponse;
import com.be.java.foxbase.dto.response.ZaloPayOrderResponse;
import com.be.java.foxbase.dto.response.ZaloPayPaymentStatusResponse;
import com.be.java.foxbase.dto.zalopay.ZaloPayOrder;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.repository.BookRepository;
import com.be.java.foxbase.repository.OrderRepository;
import com.be.java.foxbase.repository.PaymentTransactionRepository;
import com.be.java.foxbase.repository.PurchasedBookRepository;
import com.be.java.foxbase.repository.UserRepository;
import com.be.java.foxbase.utils.OrderStatus;
import com.be.java.foxbase.vn.zalopay.crypto.HMACUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class PurchaseService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PurchasedBookRepository purchasedBookRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private ZaloPayConfig zaloPayConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${client.domain}")
    private String CLIENT_DOMAIN;

    private final WebClient webClient = WebClient.create();

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public PurchaseWalletResponse purchaseWallet(PurchaseWalletRequest purchaseWalletRequest) {
        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        user.setBalance(user.getBalance() + purchaseWalletRequest.getAmount());

        userRepository.save(user);
        return PurchaseWalletResponse.builder()
                .success(true)
                .newBalance(user.getBalance())
                .build();
    }

    public PurchaseBookResponse purchaseBookByWallet(PurchaseBookRequest purchaseBookRequest) {
        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        Book book = bookRepository.findById(purchaseBookRequest.getBookId()).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        user.setBalance(user.getBalance() - book.getPrice());

        PurchasedBook purchasedBook = PurchasedBook.builder()
                .book(book)
                .user(user)
                .id(new UserBookId(getCurrentUsername(), purchaseBookRequest.getBookId()))
                .paid(true)
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        purchasedBookRepository.save(purchasedBook);

        return PurchaseBookResponse.builder()
                .bookTitle(book.getTitle())
                .bookPrice(book.getPrice())
                .buyer(getCurrentUsername())
                .purchaseAt(LocalDateTime.now())
                .newBalance(user.getBalance())
                .success(true)
                .build();
    }

    @Transactional
    public CreateOrderResponse createOrder(ZaloPayOrderRequest zaloPayOrderRequest) {
        String username = getCurrentUsername();
        Long bookId = zaloPayOrderRequest.getItem().getBookId();

        log.info("Creating ZaloPay order for username={}, bookId={}", username, bookId);

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        List<Order> userOrders = orderRepository.findByUser_Username(username);
        Order existingPendingOrder = userOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING && o.getBook().getBookId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (existingPendingOrder != null) {
            log.warn("User {} already has a pending order for bookId={}, appTransId={}", 
                    username, bookId, existingPendingOrder.getAppTransId());
            return null;
        }

        String appTransId = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "_" + UUID.randomUUID();
        Long appTime = System.currentTimeMillis();
        Map<String, String> embeddata = new HashMap<>() {
            {
                put("merchantinfo", "fox-base");
                put("redirecturl", CLIENT_DOMAIN + "/book/detail?id=" + bookId);
            }
        };

        ObjectNode node = objectMapper.convertValue(zaloPayOrderRequest.getItem(), ObjectNode.class);
        String item = node.toString();
        String strEmbeddata = new JSONObject(embeddata).toString();
        List<String> macList = Arrays.asList(
                String.valueOf(zaloPayConfig.getAppIdInt()),
                appTransId,
                username,
                zaloPayOrderRequest.getAmount().toString(),
                appTime.toString(),
                strEmbeddata,
                item
        );

        String macInput = String.join("|", macList);
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zaloPayConfig.getKey1(), macInput);

        log.info("Generated MAC for appTransId={}", appTransId);
        Order order = Order.builder()
                .appTransId(appTransId)
                .user(user)
                .book(book)
                .amount(zaloPayOrderRequest.getAmount())
                .appTime(appTime)
                .embedData(strEmbeddata)
                .item(item)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .webhookReceived(false)
                .queryAttempts(0)
                .build();

        orderRepository.save(order);
        UserBookId userBookId = new UserBookId(username, bookId);
        PurchasedBook purchasedBook = purchasedBookRepository.findById(userBookId)
                .orElse(PurchasedBook.builder()
                        .id(userBookId)
                        .user(user)
                        .book(book)
                        .paid(false)
                        .createdAt(LocalDateTime.now())
                        .paidAt(null)
                        .build());

        purchasedBook.setPaid(false);
        purchasedBookRepository.save(purchasedBook);
        ZaloPayOrder orderRequest = ZaloPayOrder.builder()
                .appid(zaloPayConfig.getAppIdInt())
                .appuser(username)
                .apptime(appTime)
                .amount(zaloPayOrderRequest.getAmount())
                .apptransid(appTransId)
                .item(item)
                .embeddata(strEmbeddata)
                .mac(mac)
                .build();
        ZaloPayOrderResponse response;
        try {
            response = sendOrderRequest(orderRequest);
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .appTransId(appTransId)
                    .source("create_order")
                    .requestData(toJson(orderRequest))
                    .responseData(toJson(response))
                    .returnCode(response.getReturncode())
                    .returnMessage(response.getReturnmessage())
                    .macValid(true)
                    .build();
            paymentTransactionRepository.save(transaction);

            log.info("ZaloPay order created successfully for appTransId={}, returnCode={}", 
                    appTransId, response.getReturncode());

        } catch (Exception e) {
            log.error("Error creating ZaloPay order for appTransId={}: {}", appTransId, e.getMessage(), e);
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .appTransId(appTransId)
                    .source("create_order")
                    .requestData(toJson(orderRequest))
                    .errorMessage(e.getMessage())
                .build();
            paymentTransactionRepository.save(transaction);

            // Cập nhật order status
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Failed to create order: " + e.getMessage());
            orderRepository.save(order);

            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        return CreateOrderResponse.builder()
                .appTransId(appTransId)
                .zaloPayResponse(response)
                .build();
    }

    private ZaloPayOrderResponse sendOrderRequest(ZaloPayOrder orderRequest) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("appid", String.valueOf(orderRequest.getAppid()));
        formData.add("apptransid", orderRequest.getApptransid());
        formData.add("appuser", orderRequest.getAppuser());
        formData.add("amount", String.valueOf(orderRequest.getAmount()));
        formData.add("apptime", String.valueOf(orderRequest.getApptime()));
        formData.add("embeddata", orderRequest.getEmbeddata());
        formData.add("item", orderRequest.getItem());
        formData.add("mac", orderRequest.getMac());

        return webClient.post()
                .uri(zaloPayConfig.getCreateOrderEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(ZaloPayOrderResponse.class)
                .block();
    }

    public ZaloPayPaymentStatusResponse queryPaymentStatus(String appTransId) {
        log.info("Querying payment status for appTransId={}", appTransId);

        Order order = orderRepository.findByAppTransId(appTransId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
        String macInput = zaloPayConfig.getAppIdInt() + "|" + appTransId + "|" + zaloPayConfig.getKey1();
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zaloPayConfig.getKey1(), macInput);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("appid", String.valueOf(zaloPayConfig.getAppIdInt()));
        params.add("apptransid", appTransId);
        params.add("mac", mac);

        try {
            ZaloPayPaymentStatusResponse response = webClient.post()
                    .uri(zaloPayConfig.getStatusQueryEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(ZaloPayPaymentStatusResponse.class)
                    .block();
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .appTransId(appTransId)
                    .source("scheduled_query")
                    .requestData(toJson(params))
                    .responseData(toJson(response))
                    .returnCode(response.getReturncode())
                    .returnMessage(response.getReturnmessage())
                    .zpTransId(response.getZptransid())
                    .macValid(true)
                    .build();
            paymentTransactionRepository.save(transaction);
            updateOrderStatusFromResponse(order, response);

            log.info("Payment status queried for appTransId={}, returnCode={}", appTransId, response.getReturncode());

            return response;

        } catch (Exception e) {
            log.error("Error querying payment status for appTransId={}: {}", appTransId, e.getMessage(), e);
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .appTransId(appTransId)
                    .source("scheduled_query")
                    .requestData(toJson(params))
                    .errorMessage(e.getMessage())
                    .build();
            paymentTransactionRepository.save(transaction);

            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Transactional
    public void updateOrderStatusFromResponse(Order order, ZaloPayPaymentStatusResponse response) {
        if (order.getStatus() == OrderStatus.PAID) {
            log.debug("Order {} already PAID, skipping status update", order.getAppTransId());
            return;
        }

        Integer returnCode = response.getReturncode();
        OrderStatus newStatus = mapReturnCodeToStatus(returnCode);

        order.setZpTransId(response.getZptransid());
        order.setQueryAttempts(order.getQueryAttempts() + 1);

        if (newStatus == OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            updatePurchasedBook(order);
        } else if (newStatus == OrderStatus.FAILED) {
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Payment failed - return code: " + returnCode);
        } else if (newStatus == OrderStatus.EXPIRED) {
            order.setStatus(OrderStatus.EXPIRED);
            order.setExpiredAt(LocalDateTime.now());
        }

        orderRepository.save(order);
        log.info("Updated order status for appTransId={}: {} -> {}", order.getAppTransId(), order.getStatus(), newStatus);
    }

    private OrderStatus mapReturnCodeToStatus(Integer returnCode) {
        if (returnCode == null) {
            return OrderStatus.PENDING;
        }

        switch (returnCode) {
            case 1:
                return OrderStatus.PAID;
            case 2:
                return OrderStatus.FAILED;
            case -51:
                return OrderStatus.EXPIRED;
            default:
                log.warn("Unknown ZaloPay return code: {}", returnCode);
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

    @Scheduled(fixedRate = 300000)
    public void scheduledQueryPendingOrders() {
        log.debug("Running scheduled query for pending orders");

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Order> pendingOrders = orderRepository.findPendingOrdersForQuery(
                OrderStatus.PENDING,
                12,
                oneHourAgo
        );

        log.info("Found {} pending orders to query", pendingOrders.size());

        for (Order order : pendingOrders) {
            try {
                queryPaymentStatus(order.getAppTransId());
            } catch (Exception e) {
                log.error("Error querying status for appTransId={}: {}", order.getAppTransId(), e.getMessage(), e);
            }
        }

        updateExpiredOrders();
    }

    private void updateExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredOrders(OrderStatus.PENDING, LocalDateTime.now());

        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.EXPIRED);
            order.setExpiredAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Updated expired order: appTransId={}", order.getAppTransId());
        }
    }

    public PurchaseBookResponse checkPaymentStatus(Long bookId) {
        String username = getCurrentUsername();
        
        Order order = orderRepository.findByUser_Username(username).stream()
                .filter(o -> o.getBook().getBookId().equals(bookId))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .findFirst()
                .orElse(null);

        if (order == null) {
            PurchasedBook purchasedBook = purchasedBookRepository.findById(new UserBookId(username, bookId))
                    .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

            User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        return PurchaseBookResponse.builder()
                .success(purchasedBook.isPaid())
                .bookTitle(purchasedBook.getBook().getTitle())
                .bookPrice(purchasedBook.getBook().getPrice())
                .newBalance(user.getBalance())
                .purchaseAt(purchasedBook.getPaidAt())
                    .buyer(username)
                    .build();
        }

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        boolean isPaid = order.getStatus() == OrderStatus.PAID;

        return PurchaseBookResponse.builder()
                .success(isPaid)
                .bookTitle(order.getBook().getTitle())
                .bookPrice(order.getBook().getPrice())
                .newBalance(user.getBalance())
                .purchaseAt(order.getPaidAt())
                .buyer(username)
                .build();
    }

    public OrderStatusResponse getOrderStatusResponse(String appTransId) {
        Order order = orderRepository.findByAppTransId(appTransId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        return OrderStatusResponse.builder()
                .appTransId(order.getAppTransId())
                .bookId(order.getBook().getBookId())
                .bookTitle(order.getBook().getTitle())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .expiredAt(order.getExpiredAt())
                .failureReason(order.getFailureReason())
                .webhookReceived(order.getWebhookReceived())
                .queryAttempts(order.getQueryAttempts())
                .zpTransId(order.getZpTransId())
                .build();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
