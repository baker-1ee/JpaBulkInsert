package com.market.jw.loan.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterestRate {

    private BigDecimal annualRate; // 연이자율 (%)

    public InterestRate(BigDecimal annualRate) {
        if (annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("이자율은 0보다 커야 합니다.");
        }
        this.annualRate = annualRate;
    }

    /**
     * 월 이율 계산하여 반환
     *
     * @return 월 이율
     */
    public BigDecimal getMonthlyRate() {
        return annualRate.divide(BigDecimal.valueOf(12), RoundingMode.HALF_UP);
    }
}
