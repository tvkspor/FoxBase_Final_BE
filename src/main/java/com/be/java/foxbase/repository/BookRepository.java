package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.dto.response.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookId(Long bookId);
    Page<Book> findByGenreContaining(String genre, Pageable pageable);
    Page<Book> findByTitleContaining(String title, Pageable pageable);
    Page<Book> findByAuthorContaining(String author, Pageable pageable);
    List<Book> findTop6ByPriceEquals(Long price);
    List<Book> findTop6ByPriceGreaterThan(Long price);

}
