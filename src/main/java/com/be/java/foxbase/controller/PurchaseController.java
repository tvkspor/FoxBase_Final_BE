package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.PurchaseBookRequest;
import com.be.java.foxbase.dto.request.PurchaseWalletRequest;
import com.be.java.foxbase.dto.request.ZaloPayOrderRequest;
import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.CreateOrderResponse;
import com.be.java.foxbase.dto.response.OrderStatusResponse;
import com.be.java.foxbase.dto.response.PurchaseBookResponse;
import com.be.java.foxbase.dto.response.PurchaseWalletResponse;
import com.be.java.foxbase.dto.response.ZaloPayOrderResponse;
import com.be.java.foxbase.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    @PostMapping("book/zalo-pay/create-order")
    ApiResponse<CreateOrderResponse> createZaloPayOrder(@RequestBody ZaloPayOrderRequest zaloPayOrderRequest) {
        var response = purchaseService.createOrder(zaloPayOrderRequest);

        if (response == null){
            return ApiResponse.<CreateOrderResponse>builder()
                    .data(null)
                    .message("Please pay your current order before creating a new one.")
                    .build();
        }
        
        return ApiResponse.<CreateOrderResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("/book/fox-budget")
    public ApiResponse<PurchaseBookResponse> purchaseBook(@RequestBody PurchaseBookRequest request) {
        return ApiResponse.<PurchaseBookResponse>builder()
                .data(purchaseService.purchaseBookByWallet(request))
                .build();
    }

    @PostMapping("/wallet")
    public ApiResponse<PurchaseWalletResponse> purchaseWallet(@RequestBody PurchaseWalletRequest request) {
        return ApiResponse.<PurchaseWalletResponse>builder()
                .data(purchaseService.purchaseWallet(request))
                .build();
    }

    @GetMapping("/book/zalo-pay/check-status")
    public ApiResponse<PurchaseBookResponse> checkPaymentStatus(
            @RequestParam Long bookId
    ){
        return ApiResponse.<PurchaseBookResponse>builder()
                .data(purchaseService.checkPaymentStatus(bookId))
                .build();
    }

    @GetMapping("/book/zalo-pay/order-status")
    public ApiResponse<OrderStatusResponse> getOrderStatus(
            @RequestParam String appTransId
    ){
        return ApiResponse.<OrderStatusResponse>builder()
                .data(purchaseService.getOrderStatusResponse(appTransId))
                .build();
    }

}
