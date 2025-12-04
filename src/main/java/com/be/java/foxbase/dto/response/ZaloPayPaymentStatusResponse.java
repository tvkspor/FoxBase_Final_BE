package com.be.java.foxbase.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZaloPayPaymentStatusResponse {
    Integer returncode;
    String returnmessage;
    boolean isprocessing;
    Long amount;
    Long discountamount;
    String zptransid;
}

