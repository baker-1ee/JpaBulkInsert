package com.jw.domain.mysql.loan.repository;

import com.jw.domain.mysql.loan.entity.MyLoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyLoanRepository extends JpaRepository<MyLoan, Long> {
}
