package com.example.bookstore.domain.book.service

import com.example.bookstore.domain.book.entity.Book
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookRegisterServiceJTest {

    @Test
    fun `should successfully register a book`() {
        val book = Book(
            bookSeq = 3L,
            title = "Some title",
            author = "Some author"
        )
    }

}
