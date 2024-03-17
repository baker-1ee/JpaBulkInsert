package com.example.jpabulkinsert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookRegisterService {

    private final BookRepository bookRepository;

    @Transactional
    public List<Book> register(List<BookSaveDto> dtos) {

        List<Book> newBooks = dtos.stream()
                .map(Book::from)
                .collect(Collectors.toList());

        return bookRepository.saveAll(newBooks);
    }

}
