package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByAppTransIdOrderByCreatedAtDesc(String appTransId);
}

