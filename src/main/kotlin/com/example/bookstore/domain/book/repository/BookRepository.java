package com.example.bookstore.domain.book.repository;

import com.example.bookstore.domain.book.entity.BookJ;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookJ, Long> {

}
