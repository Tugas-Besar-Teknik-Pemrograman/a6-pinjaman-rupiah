package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.valueobject.Money;

public class WithdrawalService {

    private final LenderRepository lenderRepository;
    private final BorrowerRepository borrowerRepository;

    public WithdrawalService(LenderRepository lenderRepository, BorrowerRepository borrowerRepository) {
        this.lenderRepository = lenderRepository;
        this.borrowerRepository = borrowerRepository;
    }

    public void withdraw(LenderId lenderId, Money amount) {
        Lender lender = lenderRepository.findById(lenderId);
        if (lender == null) {
            throw new IllegalArgumentException("Lender tidak ditemukan");
        }

        lender.tarikSaldo(amount);

        lenderRepository.save(lender);
    }

    public void withdraw(BorrowerId borrowerId, Money amount) {
        Borrower borrower = borrowerRepository.findById(borrowerId);
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower tidak ditemukan");
        }

        borrower.tarikSaldo(amount);

        borrowerRepository.save(borrower);
    }
}
