package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.key.UserBookRatingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UserBookRatingId> {
    @EntityGraph(attributePaths = { "user" })
    Page<Rating> findByBook_BookId(Long id, Pageable pageable);

    Optional<Rating> findByUserBookRatingId(UserBookRatingId id);

    List<Rating> findByBook_BookId(Long id);

    long countByBook_BookId(Long id);

    @Query("SELECT ROUND(AVG(r.rate), 1) FROM Rating r WHERE r.book.bookId = :id")
    Double findBookAverageRating(@Param("id") Long id);
}