package com.jw.domain.oracle.loan.repository;

import com.jw.domain.oracle.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
