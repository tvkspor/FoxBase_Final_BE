package com.be.java.foxbase.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookCreationRequest {
    String title;
    String author;
    String description;
    String contentUrl;
    String imageUrl;
    String genre;
    Long price;
}
