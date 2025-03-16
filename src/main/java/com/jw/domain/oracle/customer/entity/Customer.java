package com.jw.domain.oracle.customer.entity;

import com.jw.domain.oracle.loan.entity.CustomerLoan;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "CUSTOMER")
@SequenceGenerator(name = "CUSTOMER_SQ_GEN", sequenceName = "SQ_CUSTOMER", allocationSize = 1)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CUSTOMER_SQ_GEN")
    @Column(name = "ID")
    private Long id;

    @Column(name = "NM", nullable = false)
    private String name;

    @OneToMany(mappedBy = "customer")
    private List<CustomerLoan> customerLoans;


    public static Customer only(String name) {
        return Customer.builder()
                .name(name)
                .build();
    }

    public static Customer of(Long id, String name) {
        return Customer.builder()
                .id(id)
                .name(name)
                .build();
    }

    public static Customer withLoans(String name, List<CustomerLoan> customerLoans) {
        return Customer.builder()
                .name(name)
                .customerLoans(customerLoans)
                .build();
    }
}
