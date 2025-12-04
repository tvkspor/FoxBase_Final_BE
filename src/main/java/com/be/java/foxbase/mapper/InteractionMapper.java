package com.be.java.foxbase.mapper;

import com.be.java.foxbase.db.entity.Interaction;
import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.InteractionId;
import com.be.java.foxbase.dto.request.InteractionRequest;
import com.be.java.foxbase.dto.response.InteractionResponse;
import org.springframework.stereotype.Component;

@Component
public class InteractionMapper {
    public InteractionResponse toInteractionResponse(Interaction interaction) {
        return InteractionResponse.builder()
                .interactUsername(interaction.getId().getInteractUsername())
                .ratedBookId(interaction.getId().getRatedBookId())
                .creatorUsername(interaction.getId().getCreatorUsername())
                .action(interaction.getAction())
                .build();
    }

    public Interaction toInteraction(InteractionRequest interactionRequest, User interactUser, Rating rating) {
        return Interaction.builder()
                .id(new InteractionId(interactUser.getUsername(), interactionRequest.getCreatorUsername(), interactionRequest.getRatedBookId()))
                .action(interactionRequest.getAction())
                .rating(rating)
                .interact(interactUser)
                .build();
    }
}
