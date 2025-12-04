package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.Interaction;
import com.be.java.foxbase.db.key.InteractionId;
import com.be.java.foxbase.utils.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, InteractionId> {
    @Query("SELECT COUNT(i.action) FROM Interaction i " +
            "WHERE i.action = :action " +
            "AND i.id.creatorUsername = :creator " +
            "AND i.id.ratedBookId = :bookId")
    int countInteraction(@Param("action") InteractionType action,
                         @Param("creator") String creator,
                         @Param("bookId") Long bookId);
}
