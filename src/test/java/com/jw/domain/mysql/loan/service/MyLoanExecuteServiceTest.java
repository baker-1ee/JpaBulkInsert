package com.jw.domain.mysql.loan.service;

import com.jw.domain.mysql.loan.vo.LoanSaveVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
@Rollback(false)
class MyLoanExecuteServiceTest {

    private final int DATA_SIZE = 100000;

    @Autowired
    private MyLoanExecuteService myLoanExecuteService;

    @Test
    @Transactional(transactionManager = "mysqlTransactionManager")
    void testPerformance() {
        LoanSaveVo vo = LoanSaveVo.builder()
                .amount(BigDecimal.valueOf(100_000_000))
                .interestRate(BigDecimal.valueOf(4.0))
                .durationMonths(DATA_SIZE)
                .build();
        long start = System.currentTimeMillis();
        myLoanExecuteService.executeCreditLoan(vo);
        long end = System.currentTimeMillis();
        log.info("MyLoanExecuteService cost time: {} ms", end - start);
    }

    @Test
    @Transactional(transactionManager = "mysqlTransactionManager")
    void createCreditLoan() {
        myLoanExecuteService.executeCreditLoan(
                LoanSaveVo.builder()
                        .amount(BigDecimal.valueOf(10_000_000))
                        .interestRate(BigDecimal.valueOf(3.0))
                        .durationMonths(3)
                        .build()
        );
        myLoanExecuteService.executeCreditLoan(
                LoanSaveVo.builder()
                        .amount(BigDecimal.valueOf(2_000_000))
                        .interestRate(BigDecimal.valueOf(2.0))
                        .durationMonths(2)
                        .build()
        );
    }
}
