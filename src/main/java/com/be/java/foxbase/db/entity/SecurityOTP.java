package com.be.java.foxbase.db.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SecurityOTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    int otp;
    LocalDateTime otpExpiryTime;

    String resetToken;
    LocalDateTime tokenExpiryTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    User user;
}
