package com.example.bookstore.domain.book.entity

import com.example.bookstore.domain.book.dto.BookSaveDto
import java.util.*

class BookFactory {
    companion object {
        fun from(dto: BookSaveDto): Book {
            return Book(
                bookSeq = UUID.randomUUID().mostSignificantBits,
                title = dto.title,
                author = dto.author,
                publicationDate = dto.publicationDate,
                price = dto.price
            )
        }
    }
}
