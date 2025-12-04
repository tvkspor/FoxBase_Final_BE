package com.be.java.foxbase.db.entity;

import com.be.java.foxbase.db.key.UserBookRatingId;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rating {
    @EmbeddedId
    UserBookRatingId userBookRatingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("creatorUsername")
    @JoinColumn(name = "creator_username")
    @NotFound(action = NotFoundAction.IGNORE)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ratedBookId")
    @JoinColumn(name = "rated_book_id")
    Book book;

    Double rate;
    Integer likes;
    Integer dislikes;
    Integer loves;

    String comment;
    LocalDateTime createdAt;
}
