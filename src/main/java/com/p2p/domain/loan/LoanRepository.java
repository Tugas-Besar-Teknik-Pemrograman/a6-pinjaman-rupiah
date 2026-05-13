package com.p2p.domain.loan;

public interface LoanRepository {
    Loan findById(LoanId loanId);
    void save(Loan loan);
}
