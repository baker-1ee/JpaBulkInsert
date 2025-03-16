package com.jw.domain.oracle.loan.repository;

import com.jw.domain.oracle.loan.entity.CustomerLoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerLoanRepositoryOracle extends JpaRepository<CustomerLoan, Long> {
}
