package com.jw.domain.oracle.customer;


import com.jw.domain.oracle.customer.entity.Customer;
import com.jw.domain.oracle.customer.repository.CustomerRepositoryOracle;
import com.jw.domain.oracle.customer.service.CustomerBatchService;
import com.jw.domain.oracle.loan.repository.CustomerLoanRepositoryOracle;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
@Transactional(transactionManager = "oracleTransactionManager")
@Rollback(false)
class CustomerLoanSaveTest {

    private final int DATA_SIZE = 100000;

    @Autowired
    private CustomerBatchService customerBatchService;
    @Autowired
    private CustomerRepositoryOracle customerRepository;
    @Autowired
    private CustomerLoanRepositoryOracle loanRepository;

    @Test
    void test() {
        List<Customer> customers = IntStream.rangeClosed(1, DATA_SIZE)
                .mapToObj(i -> Customer.only("홍길동" + i))
                .collect(Collectors.toList());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Batch Insert");

        customerRepository.saveAll(customers);
        customerRepository.flush();

        stopWatch.stop();
        log.info("size : {}, elapsed time : {} ms)", DATA_SIZE, stopWatch.getLastTaskTimeMillis());
    }

    @Test
    void testNativeQuery() {
        List<Customer> customers = IntStream.rangeClosed(1, DATA_SIZE)
                .mapToObj(i -> Customer.of((long) i, "홍길동" + i))
                .collect(Collectors.toList());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Batch Insert");

        customerBatchService.batchInsertUsingInsertAll(customers);

        stopWatch.stop();
        log.info("size : {}, elapsed time : {} ms)", DATA_SIZE, stopWatch.getLastTaskTimeMillis());
    }
}
