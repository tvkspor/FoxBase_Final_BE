package com.be.java.foxbase.dto.request;

import com.be.java.foxbase.dto.zalopay.BookItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZaloPayOrderRequest {
    Long amount;
    BookItem item;
}

