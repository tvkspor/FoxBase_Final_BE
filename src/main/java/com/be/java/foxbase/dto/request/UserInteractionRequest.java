package com.be.java.foxbase.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInteractionRequest {
    String creatorUsername;
    Long ratedBookId;
}
