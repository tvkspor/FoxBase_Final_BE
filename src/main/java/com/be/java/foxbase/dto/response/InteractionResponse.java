package com.be.java.foxbase.dto.response;

import com.be.java.foxbase.utils.InteractionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InteractionResponse {
    String interactUsername;
    String creatorUsername;
    Long ratedBookId;

    @Enumerated(EnumType.STRING)
    InteractionType action;
}
