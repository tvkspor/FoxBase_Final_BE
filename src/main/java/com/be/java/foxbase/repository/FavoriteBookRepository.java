package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.FavoriteBook;
import com.be.java.foxbase.db.key.UserBookId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteBookRepository extends JpaRepository<FavoriteBook, UserBookId> {
    List<FavoriteBook> findByUser_Username(String username);
}
