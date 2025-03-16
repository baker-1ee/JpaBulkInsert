package com.jw.domain.oracle.customer;


import com.jw.domain.oracle.customer.entity.Customer;
import com.jw.domain.oracle.customer.repository.CustomerRepositoryOracle;
import com.jw.domain.oracle.loan.repository.CustomerLoanRepositoryOracle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
@Transactional(transactionManager = "oracleTransactionManager")
@Rollback(false)
class CustomerLoanSaveTest {

    @Autowired
    private CustomerRepositoryOracle customerRepository;
    @Autowired
    private CustomerLoanRepositoryOracle loanRepository;

    @Test
    void test() {
        List<Customer> customers = IntStream.range(0, 2)
                .mapToObj(i -> Customer.only("홍길동" + i))
                .collect(Collectors.toList());
        log.info("customers size: {}", customers.size());
        customerRepository.saveAll(customers); // 한 번에 저장
    }
}
