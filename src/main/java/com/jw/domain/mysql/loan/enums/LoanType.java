package com.jw.domain.mysql.loan.enums;

import com.jw.common.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoanType implements BaseEnum {

    CREDIT_LOAN("CL", "신용대출"),
    MORTGAGE_LOAN("ML", "주택담보대출"),
    AUTO_LOAN("AL", "자동차담보대출");

    private final String code;
    private final String name;

    public static LoanType fromCode(String code) {
        return BaseEnum.fromCode(LoanType.class, code);
    }

}
