package com.capital.jw.loan.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanAmount {

    private BigDecimal requestedAmount;  // 신청 금액
    private BigDecimal maxLimit;  // 고객 최대 한도

    public LoanAmount(BigDecimal requestedAmount, BigDecimal maxLimit) {
        if (requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("대출 신청 금액은 0보다 커야 합니다.");
        }
        if (requestedAmount.compareTo(maxLimit) > 0) {
            throw new IllegalArgumentException("대출 신청 금액이 최대 한도를 초과할 수 없습니다.");
        }
        this.requestedAmount = requestedAmount;
        this.maxLimit = maxLimit;
    }
}
