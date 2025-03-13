package com.example.jpabulkinsert;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoIncrementedIdBookRepository extends JpaRepository<AutoIncrementedIdBookEntity, Long> {

}
