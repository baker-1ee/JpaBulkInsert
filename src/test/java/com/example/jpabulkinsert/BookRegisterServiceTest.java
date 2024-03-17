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
class BookRegisterServiceTest {

    private final int DATA_SIZE = 10000;

    @Autowired
    private BookRegisterService bookRegisterService;

    @Test
    void bulkInsertTest() {
        // given
        List<BookSaveDto> dtos = getBulkData();

        // when
        long startTime = System.currentTimeMillis();
        List<Book> actual = bookRegisterService.register(dtos);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        long elapsedTime = endTime - startTime;
        System.out.println("elapsedTime = " + elapsedTime);
        assertThat(elapsedTime).isLessThan(2000);
    }

    private List<BookSaveDto> getBulkData() {
        return IntStream.rangeClosed(1, DATA_SIZE)
                .mapToObj(i -> BookSaveDto
                        .builder()
                        .title("jpa bulk insert")
                        .author("john")
                        .publicationDate(LocalDateTime.now())
                        .price(BigDecimal.valueOf(i))
                        .build())
                .collect(Collectors.toList());
    }
}
