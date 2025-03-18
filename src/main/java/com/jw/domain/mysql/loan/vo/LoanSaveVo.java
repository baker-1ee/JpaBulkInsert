package com.jw.domain.mysql.loan.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Getter
public class LoanSaveVo {
    private BigDecimal amount;
    private BigDecimal interestRate;
    private int durationMonths;
}
