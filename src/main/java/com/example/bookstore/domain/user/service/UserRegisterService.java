package com.example.bookstore.domain.user.service;

import com.example.bookstore.domain.user.dto.UserSaveDto;
import com.example.bookstore.domain.user.entity.User;
import com.example.bookstore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRegisterService {

    private final UserRepository userRepository;

    public List<User> register(List<UserSaveDto> dtos) {
        List<User> newUsers = dtos.stream()
                .map(User::from)
                .collect(Collectors.toList());

        newUsers.add(User.builder().userSeq(8786171922801838528L).build());

        return userRepository.saveAll(newUsers);
    }

}
