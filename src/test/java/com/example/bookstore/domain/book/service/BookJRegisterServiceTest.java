package com.example.bookstore.domain.book.service;

import com.example.bookstore.domain.book.dto.BookModifyDto;
import com.example.bookstore.domain.book.dto.BookSaveDto;
import com.example.bookstore.domain.book.entity.BookJ;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookJRegisterServiceTest {

    private final int DATA_SIZE = 3;

    @Autowired
    private BookRegisterServiceJ bookRegisterServiceJ;
    @Autowired
    private BookModifyServiceJ bookModifyServiceJ;

    @Test
    @Rollback(value = false)
    void bulkInsertTest() {
        // given
        List<BookSaveDto> dtos = getBulkData();

        // when
        List<BookJ> actual = bookRegisterServiceJ.register(dtos);

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
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

    @Test
    @Rollback(value = false)
    void updateTest() {
        // given
        BookModifyDto dto = BookModifyDto.builder().bookSeq(463185878156529487L).price(BigDecimal.valueOf(50000)).build();

        // when
        bookModifyServiceJ.modify(dto);
    }

}
