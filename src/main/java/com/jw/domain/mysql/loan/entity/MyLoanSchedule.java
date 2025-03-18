package com.jw.domain.mysql.loan.entity;

import com.jw.common.entity.BulkInsertEntity;
import com.jw.common.id.SequenceHolder;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "LOAN_SCHEDULE")
public class MyLoanSchedule extends BulkInsertEntity<Long> {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LOAN_ID", nullable = false)
    private MyLoan loan;

    @Column(name = "INSTALLMENT_NO", nullable = false)
    private int installmentNo;

    @Column(name = "PAYMENT_AMOUNT", nullable = false)
    private BigDecimal paymentAmount;

    // 대출 상환 일정 생성 팩토리 메서드
    public static MyLoanSchedule of(MyLoan loan, int installmentNo, BigDecimal paymentAmount) {
        return MyLoanSchedule.builder()
                .id(SequenceHolder.nextValue())
                .loan(loan)
                .installmentNo(installmentNo)
                .paymentAmount(paymentAmount)
                .build();
    }
}
