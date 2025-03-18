package com.jw.domain.mysql.loan.service;

import com.jw.domain.mysql.loan.entity.MyLoan;
import com.jw.domain.mysql.loan.repository.MyLoanRepository;
import com.jw.domain.mysql.loan.vo.LoanSaveVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyLoanExecuteService {

    private final MyLoanRepository myLoanRepository;

    @Transactional(transactionManager = "mysqlTransactionManager")
    public MyLoan executeCreditLoan(LoanSaveVo vo) {
        MyLoan myLoan = MyLoan.createCreditLoan(vo);
        MyLoan save = myLoanRepository.save(myLoan);
        myLoanRepository.flush();
        return save;
    }

}
