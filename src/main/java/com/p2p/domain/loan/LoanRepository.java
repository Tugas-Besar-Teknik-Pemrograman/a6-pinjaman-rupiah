package com.p2p.domain.loan;

public interface LoanRepository {
    Loan findById(String id);
    void save(Loan loan);
}
