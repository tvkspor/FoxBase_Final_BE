package com.be.java.foxbase.db.entity;

import com.be.java.foxbase.db.key.InteractionId;
import com.be.java.foxbase.utils.InteractionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Interaction {
    @EmbeddedId
    InteractionId id;

    @ManyToOne
    @MapsId("interactUsername")
    @JoinColumn(name = "interact_username")
    User interact;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "creatorUsername", referencedColumnName = "creator_username", insertable = false, updatable = false),
            @JoinColumn(name = "ratedBookId", referencedColumnName = "rated_book_id", insertable = false, updatable = false)
    })
    Rating rating;

    @Enumerated(EnumType.STRING)
    InteractionType action;
}


