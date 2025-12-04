package com.be.java.foxbase.mapper;

import com.be.java.foxbase.dto.request.BookCreationRequest;
import com.be.java.foxbase.dto.response.BookResponse;
import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    public BookResponse toBookResponse(Book book) {
        return BookResponse.builder().
                bookId(book.getBookId()).
                title(book.getTitle()).
                author(book.getAuthor()).
                genre(book.getGenre()).
                description(book.getDescription()).
                price(book.getPrice()).
                averageRating(book.getAverageRating()).
                contentUrl(book.getContentUrl()).
                imageUrl(book.getImageUrl()).
                build();
    }

    public Book toBook(BookCreationRequest request) {
        return Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .genre(request.getGenre())
                .description(request.getDescription())
                .price(request.getPrice())
                .averageRating(0.0)
                .contentUrl(request.getContentUrl())
                .imageUrl(request.getImageUrl())
                .build();
    }
}
