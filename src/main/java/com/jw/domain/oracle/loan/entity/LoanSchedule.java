package com.jw.domain.oracle.loan.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "LOAN_SCHEDULE")
@SequenceGenerator(name = "LOAN_SCHEDULE_SQ_GEN", sequenceName = "SQ_LOAN_SCHEDULE", allocationSize = 1)
public class LoanSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOAN_SCHEDULE_SQ_GEN")
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LOAN_ID", nullable = false)
    private Loan loan;

    @Column(name = "INSTALLMENT_NO", nullable = false)
    private int installmentNo;

    @Column(name = "PAYMENT_AMOUNT", nullable = false)
    private BigDecimal paymentAmount;

    // 대출 상환 일정 생성 팩토리 메서드
    public static LoanSchedule of(Loan loan, int installmentNo, BigDecimal paymentAmount) {
        return LoanSchedule.builder()
                .loan(loan)
                .installmentNo(installmentNo)
                .paymentAmount(paymentAmount)
                .build();
    }
}
