package com.be.java.foxbase.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {
    String creatorUsername;
    String creatorFName;
    String creatorLName;
    String creatorAvatar;
    Long ratedBookId;
    Double rate;
    Integer likes;
    Integer dislikes;
    Integer loves;
    String comment;
    LocalDateTime createdAt;
}
