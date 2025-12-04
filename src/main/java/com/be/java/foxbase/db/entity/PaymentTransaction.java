package com.be.java.foxbase.db.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String appTransId;

    @Column(nullable = false, length = 50)
    String source;

    @Column(length = 500)
    String requestData;

    @Column(length = 2000)
    String responseData;

    @Column
    Integer returnCode;

    @Column(length = 500)
    String returnMessage;

    @Column(length = 100)
    String zpTransId;

    @Column
    Boolean macValid;

    @Column(length = 50)
    String ipAddress;

    @Column(nullable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 1000)
    String errorMessage;
}

