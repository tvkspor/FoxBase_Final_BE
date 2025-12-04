package com.be.java.foxbase.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    Long bookId;
    String title;
    String author;
    String description;
    String contentUrl;
    String imageUrl;
    String genre;
    Long price;
    Double averageRating;
}
