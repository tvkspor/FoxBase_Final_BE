package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookRatingId;
import com.be.java.foxbase.dto.request.RatingRequest;
import com.be.java.foxbase.dto.response.PaginatedResponse;
import com.be.java.foxbase.dto.response.RatingResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.mapper.RatingMapper;
import com.be.java.foxbase.repository.BookRepository;
import com.be.java.foxbase.repository.RatingRepository;
import com.be.java.foxbase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RatingService {
    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    RatingMapper ratingMapper;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public PaginatedResponse<RatingResponse> getBookRatings(Long bookId, Pageable pageable) {
        try {
            var ratings = ratingRepository.findByBook_BookId(bookId, pageable);
            var responses = ratings.getContent().stream().map(item -> {
                // Avoid touching item.getUser() to prevent lazy-load of missing user
                String creator = item.getUserBookRatingId() != null ? item.getUserBookRatingId().getCreatorUsername()
                        : null;
                User user = creator != null ? userRepository.findByUsername(creator).orElse(null) : null;
                return ratingMapper.toRatingResponse(item, user);
            }).toList();
            return PaginatedResponse.<RatingResponse>builder()
                    .content(responses)
                    .totalElements(ratings.getTotalElements())
                    .totalPages(ratings.getTotalPages())
                    .size(ratings.getSize())
                    .page(ratings.getNumber())
                    .build();
        } catch (Exception e) {
            System.out.println("Repository error: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public RatingResponse createRating(RatingRequest ratingRequest) {
        if (ratingRequest.getRate() < 0 || ratingRequest.getRate() > 5) {
            throw new AppException(ErrorCode.INVALID_RATE);
        }

        if (ratingRequest.getComment() != null && ratingRequest.getComment().length() > 100) {
            throw new AppException(ErrorCode.INVALID_COMMENT);
        }
        
        User creator = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST));

        Book ratedBook = bookRepository.findByBookId(ratingRequest.getRatedBookId()).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        Rating rating = ratingMapper.toRating(ratingRequest, creator, ratedBook);
        Rating saved = ratingRepository.save(rating);
        return ratingMapper.toRatingResponse(saved, creator);
    }

    public RatingResponse getMyRating(Long bookId) {
        System.out.println("Check ");
        Rating rating = ratingRepository.findByUserBookRatingId(new UserBookRatingId(getCurrentUsername(), bookId))
                .orElseThrow(
                        () -> new AppException(ErrorCode.RATING_NOT_FOUND));
        return ratingMapper.toRatingResponse(rating);
    }

    public Long countingBookRating(Long bookId) {
        return ratingRepository.countByBook_BookId(bookId);
    }
}
