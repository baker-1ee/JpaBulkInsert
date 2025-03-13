package com.example.jpabulkinsert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomIdBookEntityRegisterServiceTest {

    private final int DATA_SIZE = 3;

    @Autowired
    private BookRegisterService bookRegisterService;

    @Test
    void testAutoIncrementedIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<AutoIncrementedIdBookEntity> actual = bookRegisterService.registerAutoIncrementedIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        long elapsedTime = endTime - startTime;
        System.out.println("elapsedTime = " + elapsedTime);
    }

    @Test
    void testSequenceIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<SequenceIdBookEntity> actual = bookRegisterService.registerSequenceIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        long elapsedTime = endTime - startTime;
        System.out.println("elapsedTime = " + elapsedTime);
    }

    @Test
    void testCustomIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<CustomIdBookEntity> actual = bookRegisterService.registerCustomIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        long elapsedTime = endTime - startTime;
        System.out.println("elapsedTime = " + elapsedTime);
    }



    private List<BookSaveVo> getBookSaveVoList() {
        return IntStream.rangeClosed(1, DATA_SIZE)
                .mapToObj(i -> BookSaveVo
                        .builder()
                        .title("jpa bulk insert")
                        .author("john")
                        .publicationDate(LocalDateTime.now())
                        .price(BigDecimal.valueOf(i))
                        .build())
                .collect(Collectors.toList());
    }
}
