package com.be.java.foxbase.dto.response;

import com.be.java.foxbase.utils.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusResponse {
    String appTransId;
    Long bookId;
    String bookTitle;
    Long amount;
    OrderStatus status;
    LocalDateTime createdAt;
    LocalDateTime paidAt;
    LocalDateTime expiredAt;
    String failureReason;
    Boolean webhookReceived;
    Integer queryAttempts;
    String zpTransId;
}

