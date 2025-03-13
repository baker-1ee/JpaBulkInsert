package com.example.jpabulkinsert;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomIdBookRepository extends JpaRepository<CustomIdBookEntity, Long> {

}
