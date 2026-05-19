package com.p2p.domain.loan;

import java.util.List;

public interface LoanRepository {
    Loan findById(LoanId loanId);
    void save(Loan loan);
    List<Loan> findAll();
}
