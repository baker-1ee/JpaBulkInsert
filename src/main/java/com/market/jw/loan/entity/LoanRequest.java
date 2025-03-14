package com.market.jw.loan.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class LoanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Embedded
    private LoanAmount loanAmount;

    @Embedded
    private CreditInfo creditInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    /**
     * 대출 신청
     *
     * @param customerId 고객 아이디
     * @param loanAmount 대출 금액
     * @param creditInfo 신용 정보
     * @return 대출 신청 엔터티
     */
    public static LoanRequest create(Long customerId, LoanAmount loanAmount, CreditInfo creditInfo) {
        return LoanRequest.builder()
                .customerId(customerId)
                .loanAmount(loanAmount)
                .creditInfo(creditInfo)
                .status(LoanRequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 대출 승인
     */
    public void approve() {
        if (this.status != LoanRequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 대출 신청입니다.");
        }
        this.status = LoanRequestStatus.APPROVED;
    }

    /**
     * 대출 거절
     */
    public void reject() {
        if (this.status != LoanRequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 대출 신청입니다.");
        }
        this.status = LoanRequestStatus.REJECTED;
    }
}
