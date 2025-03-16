package com.jw.domain.mysql.loan.repository;

import com.jw.domain.mysql.loan.entity.CustomerLoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerLoanRepositoryMysql extends JpaRepository<CustomerLoan, Long> {
}
