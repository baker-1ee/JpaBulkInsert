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

    private final AutoIncrementedIdBookRepository autoIncrementedIdBookRepository;
    private final SequenceIdBookRepository sequenceIdBookRepository;
    private final CustomIdBookRepository customIdBookRepository;

    @Transactional
    public List<AutoIncrementedIdBookEntity> registerAutoIncrementedIdBooks(List<BookSaveVo> voList) {

        List<AutoIncrementedIdBookEntity> newBooks = voList.stream()
                .map(AutoIncrementedIdBookEntity::from)
                .collect(Collectors.toList());

        return autoIncrementedIdBookRepository.saveAll(newBooks);
    }

    @Transactional
    public List<SequenceIdBookEntity> registerSequenceIdBooks(List<BookSaveVo> voList) {

        List<SequenceIdBookEntity> newBooks = voList.stream()
                .map(SequenceIdBookEntity::from)
                .collect(Collectors.toList());

        return sequenceIdBookRepository.saveAll(newBooks);
    }

    @Transactional
    public List<CustomIdBookEntity> registerCustomIdBooks(List<BookSaveVo> voList) {

        List<CustomIdBookEntity> newBooks = voList.stream()
                .map(CustomIdBookEntity::from)
                .collect(Collectors.toList());

        return customIdBookRepository.saveAll(newBooks);
    }

}
