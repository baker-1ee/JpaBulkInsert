package com.jw.domain.oracle.customer.service;

import com.jw.domain.oracle.customer.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Service
public class CustomerBatchService {

    @Autowired
    @Qualifier("oracleEntityManagerFactory")
    private EntityManagerFactory oracleEntityManagerFactory;

    @Transactional(transactionManager = "oracleTransactionManager")
    public void batchInsertUsingInsertAll(List<Customer> customers) {
        if (customers.isEmpty()) {
            return;
        }

        EntityManager entityManager = oracleEntityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        StringBuilder sql = new StringBuilder("INSERT ALL ");
        for (Customer customer : customers) {
            sql.append(String.format("INTO customer (id, name) VALUES (%d, '%s') ",
                    customer.getId(), customer.getName()));
        }
        sql.append("SELECT 1 FROM DUAL");

        entityManager.createNativeQuery(sql.toString()).executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
