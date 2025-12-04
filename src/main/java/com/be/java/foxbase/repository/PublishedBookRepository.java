package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.PublishedBook;
import com.be.java.foxbase.db.key.UserBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublishedBookRepository extends JpaRepository<PublishedBook, UserBookId> {
    List<PublishedBook> findByUser_Username(String username);
    List<PublishedBook> findTop6By();
}
