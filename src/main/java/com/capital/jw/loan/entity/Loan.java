package com.capital.jw.loan.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;  // 고객 ID

    @Embedded
    private LoanAmount loanAmount; // 대출 금액 정보

    @Embedded
    private InterestRate interestRate; // 이자율 정보

    @Column(nullable = false)
    private int durationMonths;  // 대출 기간 (개월)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;  // 대출 상태

    @Column(nullable = false)
    private LocalDate startDate;  // 대출 시작일

    @Column(nullable = false)
    private LocalDate endDate;  // 만기일

    public static Loan create(Long customerId, LoanAmount loanAmount, InterestRate interestRate, int durationMonths) {
        return Loan.builder()
                .customerId(customerId)
                .loanAmount(loanAmount)
                .interestRate(interestRate)
                .durationMonths(durationMonths)
                .status(LoanStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(durationMonths))
                .build();
    }

    /**
     * 대출 종료
     */
    public void close() {
        this.status = LoanStatus.CLOSED;
    }

    /**
     * 대출의 월 납입금을 계산합니다.
     *
     * <p>계산 방식:
     * - 원리금 균등 상환 방식(Annuity Payment)을 적용합니다.
     * - 대출 원금(principalAmount)과 연이자율(interestRate)을 기반으로 월 납입금을 산출합니다.
     * - 연이자율을 12개월로 나눈 후, 월 이자를 계산하여 적용합니다.
     *
     * <p>수식:
     * - 월 납입금 = (대출 원금 × 월 이자율) + (대출 원금 ÷ 대출 기간)
     * - 월 이자율 = 연 이자율 ÷ 12
     *
     * <p>예제:
     * - 대출 금액: 5,000,000원
     * - 연 이자율: 5.5%
     * - 대출 기간: 24개월
     * - 월 납입금 = (5,000,000 × (5.5 / 12 / 100)) + (5,000,000 ÷ 24)
     *
     * @return BigDecimal 월 납입금
     */
    public BigDecimal calculateMonthlyRepayment() {
        return loanAmount.getRequestedAmount()
                .multiply(interestRate.getMonthlyRate()) // 월 이자율 적용
                .add(loanAmount.getRequestedAmount()
                        .divide(BigDecimal.valueOf(durationMonths), RoundingMode.HALF_UP)); // 원금 분할 상환
    }
}
