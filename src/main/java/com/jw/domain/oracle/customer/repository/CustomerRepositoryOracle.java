package com.jw.domain.oracle.customer.repository;

import com.jw.domain.oracle.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepositoryOracle extends JpaRepository<Customer, Long> {
}
