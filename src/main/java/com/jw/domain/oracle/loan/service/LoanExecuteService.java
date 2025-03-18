package com.jw.domain.oracle.loan.service;

import com.jw.domain.oracle.loan.entity.Loan;
import com.jw.domain.oracle.loan.repository.LoanRepository;
import com.jw.domain.oracle.loan.vo.LoanSaveVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanExecuteService {

    private final LoanRepository loanRepository;

    @Transactional(transactionManager = "oracleTransactionManager")
    public Loan executeCreditLoan(LoanSaveVo vo) {
        Loan loan = Loan.createCreditLoan(vo);
        Loan save = loanRepository.save(loan);
        loanRepository.flush();
        return save;
    }

}
