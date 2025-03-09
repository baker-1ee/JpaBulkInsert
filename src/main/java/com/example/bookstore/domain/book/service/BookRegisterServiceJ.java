package com.example.bookstore.domain.book.service;

import com.example.bookstore.domain.book.dto.BookSaveDto;
import com.example.bookstore.domain.book.entity.BookJ;
import com.example.bookstore.domain.book.repository.BookRepositoryJ;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookRegisterServiceJ {

    private final BookRepositoryJ bookRepositoryJ;

    public List<BookJ> register(List<BookSaveDto> dtos) {

        List<BookJ> newBookJS = dtos.stream()
                .map(BookJ::from)
                .collect(Collectors.toList());

        return bookRepositoryJ.saveAll(newBookJS);
    }

}
