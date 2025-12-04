package com.be.java.foxbase.dto.zalopay;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookItem {
    Long bookId;
    String title;
    String author;
    Long price;
}
