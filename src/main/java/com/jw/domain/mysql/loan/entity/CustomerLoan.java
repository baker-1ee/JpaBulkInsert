package com.jw.domain.mysql.loan.entity;

import com.jw.domain.mysql.customer.entity.Customer;
import com.jw.domain.mysql.loan.enums.LoanType;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "CUST_LOAN")
public class CustomerLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPE", nullable = false)
    private LoanType loanType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUST_ID", nullable = false)
    private Customer customer;

}
