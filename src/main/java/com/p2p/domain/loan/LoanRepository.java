package com.p2p.domain.loan;

import java.util.Optional;

public interface LoanRepository {
    Loan findById(String loanId);
    void save(Loan loan);
    Optional<Loan> findById(String id);
}