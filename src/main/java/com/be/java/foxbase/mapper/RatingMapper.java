package com.be.java.foxbase.mapper;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookRatingId;
import com.be.java.foxbase.dto.request.RatingRequest;
import com.be.java.foxbase.dto.response.RatingResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RatingMapper {
    public RatingResponse toRatingResponse(Rating rating, User user) {
        return RatingResponse.builder()
                // Use ID value to avoid forcing lazy-load of missing User
                .creatorUsername(
                        rating.getUserBookRatingId() != null ? rating.getUserBookRatingId().getCreatorUsername() : null)
                // Null-safe user fields
                .creatorFName(user != null ? user.getFName() : null)
                .creatorLName(user != null ? user.getLName() : null)
                .creatorAvatar(user != null ? user.getAvatar() : null)
                .ratedBookId(
                        rating.getUserBookRatingId() != null ? rating.getUserBookRatingId().getRatedBookId() : null)
                .rate(rating.getRate())
                .loves(rating.getLoves())
                .likes(rating.getLikes())
                .dislikes(rating.getDislikes())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    public RatingResponse toRatingResponse(Rating rating) {
        return RatingResponse.builder()
                // Use composite id to avoid lazy load of user
                .creatorUsername(
                        rating.getUserBookRatingId() != null ? rating.getUserBookRatingId().getCreatorUsername() : null)
                .ratedBookId(
                        rating.getUserBookRatingId() != null ? rating.getUserBookRatingId().getRatedBookId() : null)
                .rate(rating.getRate())
                .loves(rating.getLoves())
                .likes(rating.getLikes())
                .dislikes(rating.getDislikes())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    public Rating toRating(RatingRequest ratingRequest, User creator, Book ratedBook) {
        return Rating.builder()
                .userBookRatingId(new UserBookRatingId(creator.getUsername(), ratingRequest.getRatedBookId()))
                .user(creator)
                .book(ratedBook)
                .rate(ratingRequest.getRate())
                .likes(0)
                .dislikes(0)
                .loves(0)
                .comment(ratingRequest.getComment())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
