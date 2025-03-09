package com.example.bookstore.domain.book.service

import com.example.bookstore.domain.book.dto.BookSaveDto
import com.example.bookstore.domain.book.entity.Book
import com.example.bookstore.domain.book.entity.BookJ
import com.example.bookstore.domain.book.repository.BookRepositoryJ
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
@Transactional
class BookRegisterService {
    private val bookRepositoryJ: BookRepositoryJ? = null

    fun register(dtos: List<BookSaveDto?>): List<BookJ> {

        val book = Book(bookSeq = 3L, title = "s", author = "", price = BigDecimal.ZERO)

        val newBookJS = dtos.stream()
            .map { dto: BookSaveDto? -> BookJ.from(dto) }
            .collect(Collectors.toList())


        return bookRepositoryJ!!.saveAll(newBookJS)
    }
}
