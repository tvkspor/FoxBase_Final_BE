package com.be.java.foxbase.db.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long bookId;

    String title;
    String author;

    @Lob
    String description;

    String contentUrl;
    String imageUrl;
    String genre;
    Long price;
    Double averageRating = 0.0;
}
