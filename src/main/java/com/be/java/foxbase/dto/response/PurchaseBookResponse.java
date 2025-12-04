package com.be.java.foxbase.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseBookResponse {
    boolean success;
    String bookTitle;
    String buyer;
    Long bookPrice;
    LocalDateTime purchaseAt;
    Long newBalance;
}
