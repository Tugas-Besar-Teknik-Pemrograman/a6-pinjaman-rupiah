package com.p2p.domain.loan;

public interface LoanRepository {
    Loan findById(String loanId);
    void save(Loan loan);
}