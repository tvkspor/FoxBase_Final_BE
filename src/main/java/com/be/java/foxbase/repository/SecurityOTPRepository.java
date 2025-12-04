package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.SecurityOTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityOTPRepository extends JpaRepository<SecurityOTP, Long> {
    Optional<SecurityOTP> findByUser_username(String username);
}
