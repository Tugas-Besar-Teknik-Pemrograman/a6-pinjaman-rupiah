package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;

import java.util.UUID;

public class LoanService {
    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;

    public LoanService(BorrowerRepository borrowerRepository, LoanRepository loanRepository) {
        this.borrowerRepository = borrowerRepository;
        this.loanRepository = loanRepository;
    }

    public Loan ajukanPinjaman(String borrowerId, Money nominalPinjaman, int tenor) {
        Borrower borrower = borrowerRepository.findById(borrowerId);
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower tidak ditemukan");
        }

        Loan loanBaru = borrower.ajukanPinjaman(borrowerId, nominalPinjaman, tenor);

        borrowerRepository.save(borrower);
        loanRepository.save(loanBaru);

        return loanBaru;
    }
}

