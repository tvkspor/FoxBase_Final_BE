package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.PurchasedBook;
import com.be.java.foxbase.db.key.UserBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasedBookRepository extends JpaRepository<PurchasedBook, UserBookId> {
    List<PurchasedBook> findByUser_Username(String username);
}
