package com.jw.domain.mysql.loan.entity;

import com.jw.common.entity.BulkInsertEntity;
import com.jw.common.id.SequenceHolder;
import com.jw.domain.mysql.loan.enums.LoanType;
import com.jw.domain.mysql.loan.vo.LoanSaveVo;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "LOAN")
public class MyLoan extends BulkInsertEntity<Long> {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private LoanType type;

    @Column(name = "AMOUNT", nullable = false, scale = 25)
    private BigDecimal amount;

    @Column(name = "INTEREST_RATE", nullable = false, scale = 2, precision = 3)
    private BigDecimal interestRate;

    @Column(name = "DURATION_MONTHS", nullable = false)
    private int durationMonths;

    @Builder.Default
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MyLoanSchedule> loanSchedules = new ArrayList<>();

    // Loan 생성 시, LoanSchedule 까지 생성
    public static MyLoan createCreditLoan(LoanSaveVo vo) {
        MyLoan myLoan = MyLoan.builder()
                .id(SequenceHolder.nextValue())
                .type(LoanType.CREDIT_LOAN)
                .amount(vo.getAmount())
                .interestRate(vo.getInterestRate())
                .durationMonths(vo.getDurationMonths())
                .build();
        myLoan.generateRepaymentSchedule();
        return myLoan;
    }

    // 대출 실행 시 월별 상환 일정 자동 생성
    private void generateRepaymentSchedule() {
        BigDecimal monthlyPayment = calculateMonthlyPayment();
        IntStream.rangeClosed(1, durationMonths)
                .mapToObj(month -> MyLoanSchedule.of(this, month, monthlyPayment))
                .forEach(loanSchedules::add);
    }

    // 대출 월 상환금 계산 (원금/기간) + (월 이자)
    private BigDecimal calculateMonthlyPayment() {
        return amount.divide(valueOf(durationMonths), 5, HALF_UP)
                .add(amount.multiply(interestRate
                        .divide(valueOf(100), 5, HALF_UP)
                        .divide(valueOf(12), 5, HALF_UP)));
    }
}
