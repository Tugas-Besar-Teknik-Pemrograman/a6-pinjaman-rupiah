package com.p2p.domain.loan;

import com.p2p.domain.lender.LenderId;
import java.util.List;

public interface LoanRepository {
    Loan findById(LoanId loanId);
    void save(Loan loan);
    List<Loan> findAll();
    List<Loan> findByLenderId(LenderId lenderId);
}
