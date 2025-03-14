package com.example.jpabulkinsert;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class BookRegisterServiceTest {

    private final int DATA_SIZE = 10;

    @Autowired
    private BookRegisterService bookRegisterService;

    @Test
    @DisplayName("auto increment entity saveAll 테스트")
    void testAutoIncrementedIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<AutoIncrementedIdBookEntity> actual = bookRegisterService.registerAutoIncrementedIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        log.error("auto increment : {} ms", endTime - startTime);
    }

    @Test
    @DisplayName("sequence 채번 전략 entity saveAll 테스트")
    void testSequenceIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<SequenceIdBookEntity> actual = bookRegisterService.registerSequenceIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        log.error("sequence : {} ms", endTime - startTime);
    }

    @Test
    @DisplayName("id 직접할당 entity saveAll 테스트")
    void testCustomIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<CustomIdBookEntity> actual = bookRegisterService.registerCustomIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        log.error("id : {} ms", endTime - startTime);
    }


    private List<BookSaveVo> getBookSaveVoList() {
        return IntStream.rangeClosed(1, DATA_SIZE)
                .mapToObj(i -> BookSaveVo
                        .builder()
                        .title("Hello JPA")
                        .author("JW")
                        .publicationDate(LocalDateTime.now())
                        .price(BigDecimal.valueOf(i))
                        .build())
                .collect(Collectors.toList());
    }
}
