package com.be.java.foxbase.db.entity;

import com.be.java.foxbase.utils.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @Column(unique = true, nullable = false, length = 100)
    String appTransId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @Column(nullable = false)
    Long amount;

    @Column(nullable = false)
    Long appTime;

    @Column(length = 1000)
    String embedData;

    @Column(length = 2000)
    String item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    OrderStatus status = OrderStatus.PENDING;

    @Column(length = 100)
    String zpTransId;

    @Column(nullable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column
    LocalDateTime paidAt;

    @Column
    LocalDateTime expiredAt;

    @Column(length = 500)
    String failureReason;

    @Column(nullable = false)
    @Builder.Default
    Integer queryAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    Boolean webhookReceived = false;
}

