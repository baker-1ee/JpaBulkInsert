package com.example.bookstore.domain.user.repository;

import com.example.bookstore.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
