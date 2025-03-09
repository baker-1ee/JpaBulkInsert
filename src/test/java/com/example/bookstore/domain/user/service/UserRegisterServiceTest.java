package com.example.bookstore.domain.user.service;

import com.example.bookstore.domain.user.dto.UserSaveDto;
import com.example.bookstore.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRegisterServiceTest {

    private final int DATA_SIZE = 3;

    @Autowired
    private UserRegisterService userRegisterService;

    @Test
    @Rollback(value = false)
    void bulkInsertTest() {
        // given
        List<UserSaveDto> dtos = getBulkData();

        // when
        List<User> actual = userRegisterService.register(dtos);

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
    }

    private List<UserSaveDto> getBulkData() {
        return IntStream.rangeClosed(1, DATA_SIZE)
                .mapToObj(i -> UserSaveDto
                        .builder()
                        .name("test user")
                        .build())
                .collect(Collectors.toList());
    }
}
