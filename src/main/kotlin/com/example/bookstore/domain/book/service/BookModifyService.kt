package com.example.bookstore.domain.book.service

import com.example.bookstore.domain.book.dto.BookModifyDto
import com.example.bookstore.domain.book.repository.BookRepositoryJ
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@RequiredArgsConstructor
@Transactional
class BookModifyService {
    private val bookRepositoryJ: BookRepositoryJ? = null

    fun modify(dto: BookModifyDto) {
        val bookJ = bookRepositoryJ!!.findById(dto.bookSeq).orElseThrow {
            RuntimeException(
                "not exist entity"
            )
        }
        bookJ.updatePrice(dto.price)
    }
}
