package com.capital.jw.loan.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditInfo {

    private int creditScore; // 신용 점수
    private String creditGrade; // 신용 등급

    public CreditInfo(int creditScore) {
        if (creditScore < 300 || creditScore > 850) {
            throw new IllegalArgumentException("신용 점수는 300~850 범위여야 합니다.");
        }
        this.creditScore = creditScore;
        this.creditGrade = calculateCreditGrade(creditScore);
    }

    private String calculateCreditGrade(int score) {
        if (score >= 750) return "A";
        else if (score >= 650) return "B";
        else if (score >= 550) return "C";
        else return "D";
    }
}
