package com.jw.domain.mysql.customer.repository;

import com.jw.domain.mysql.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepositoryMysql extends JpaRepository<Customer, Long> {
}
