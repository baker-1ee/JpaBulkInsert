package com.jw.domain.mysql.customer;

import com.jw.domain.mysql.customer.entity.Customer;
import com.jw.domain.mysql.customer.repository.CustomerRepositoryMysql;
import com.jw.domain.mysql.loan.repository.CustomerLoanRepositoryMysql;
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
@Transactional(transactionManager = "mysqlTransactionManager")
@Rollback(false)
class CustomerLoanSaveTest {

    @Autowired
    private CustomerRepositoryMysql customerRepository;
    @Autowired
    private CustomerLoanRepositoryMysql loanRepository;

    @Test
    void test() {
        List<Customer> customers = IntStream.range(0, 5)
                .mapToObj(i -> Customer.only("홍길동" + i))
                .collect(Collectors.toList());
        log.info("customers size: {}", customers.size());
        customerRepository.saveAll(customers); // 한 번에 저장
        System.out.println("hi");
    }
}
