package com.p2p.infrastructure.memory;

import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryLoanRepository implements LoanRepository {
    private final Map<LoanId, Loan> loans = new HashMap<>();

    @Override
    public Loan findById(LoanId loanId) {
        return loans.get(loanId);
    }

    @Override
    public void save(Loan loan) {
        if (loan != null && loan.getId() != null) {
            loans.put(loan.getId(), loan);
        }
    }

    @Override
    public List<Loan> findAll() {
        return new ArrayList<>(loans.values());
    }

    @Override
    public List<Loan> findByLenderId(LenderId lenderId) {
        List<Loan> hasil = new ArrayList<>();
        for (Loan loan : loans.values()) {
            if (loan.getDaftarPendana().containsKey(lenderId)) {
                hasil.add(loan);
            }
        }
        return hasil;
    }

    public void clear() {
        loans.clear();
    }
}
