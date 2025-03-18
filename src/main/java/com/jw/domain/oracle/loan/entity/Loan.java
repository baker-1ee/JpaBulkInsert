package com.jw.domain.oracle.loan.entity;

import com.jw.domain.oracle.loan.enums.LoanType;
import com.jw.domain.oracle.loan.vo.LoanSaveVo;
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
@SequenceGenerator(name = "LOAN_SQ_GEN", sequenceName = "SQ_LOAN", allocationSize = 1)
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOAN_SQ_GEN")
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
    private List<LoanSchedule> loanSchedules = new ArrayList<>();

    // Loan 생성 시, LoanSchedule 까지 생성
    public static Loan createCreditLoan(LoanSaveVo vo) {
        Loan loan = Loan.builder()
                .type(LoanType.CREDIT_LOAN)
                .amount(vo.getAmount())
                .interestRate(vo.getInterestRate())
                .durationMonths(vo.getDurationMonths())
                .build();
        loan.generateRepaymentSchedule();
        return loan;
    }

    // 대출 실행 시 월별 상환 일정 자동 생성
    private void generateRepaymentSchedule() {
        BigDecimal monthlyPayment = calculateMonthlyPayment();
        IntStream.rangeClosed(1, durationMonths)
                .mapToObj(month -> LoanSchedule.of(this, month, monthlyPayment))
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
