package com.p2p.infrastructure.memory;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryLoanRepository implements LoanRepository {
    private final Map<String, Loan> loans = new HashMap<>();

    @Override
    public Loan findById(String loanId) {
        return loans.get(loanId);
    }

    @Override
    public void save(Loan loan) {
        if (loan != null && loan.getId() != null) {
            loans.put(loan.getId(), loan);
        }
    }
}
