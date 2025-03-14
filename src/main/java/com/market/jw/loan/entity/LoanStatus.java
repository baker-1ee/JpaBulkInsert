package com.market.jw.loan.entity;

public enum LoanStatus {
    PENDING,    // 대출 심사 중 (승인 전)
    ACTIVE,     // 대출 실행됨 (고객에게 지급됨)
    COMPLETED,  // 대출 전액 상환 완료
    DEFAULTED,  // 연체 발생 (지정된 기한 내에 상환하지 않음)
    CLOSED      // 대출 계약 종료 (기한 만료 후 정산 완료)
}
