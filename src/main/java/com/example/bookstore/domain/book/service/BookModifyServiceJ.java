package com.example.bookstore.domain.book.service;

import com.example.bookstore.domain.book.dto.BookModifyDto;
import com.example.bookstore.domain.book.entity.BookJ;
import com.example.bookstore.domain.book.repository.BookRepositoryJ;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookModifyServiceJ {

    private final BookRepositoryJ bookRepositoryJ;

    public void modify(BookModifyDto dto) {
        BookJ bookJ = bookRepositoryJ.findById(dto.getBookSeq()).orElseThrow(() -> new RuntimeException("not exist entity"));
        bookJ.updatePrice(dto.getPrice());
    }

}
