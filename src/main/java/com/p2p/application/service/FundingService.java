package com.p2p.application.service;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;

public class FundingService {

    private final LoanRepository loanRepository;
    private final LenderRepository lenderRepository;

    public FundingService(LoanRepository loanRepository, LenderRepository lenderRepository) {
        this.loanRepository = loanRepository;
        this.lenderRepository = lenderRepository;
    }

    public void invest(LenderId lenderId, LoanId loanId, Money amount) {
        if (amount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal investasi harus lebih dari 0");
        }

        Loan loan = loanRepository.findById(loanId);
        
        if (!"FUNDING".equals(loan.getStatus())) {
            throw new IllegalStateException("Investasi ditolak, status Loan bukan FUNDING");
        }

        Lender lender = lenderRepository.findById(lenderId);
        if (lender == null) {
            throw new IllegalArgumentException("Lender tidak ditemukan");
        }
        lender.kurangiSaldoUntukInvestasi(amount);

        loan.tambahPendanaan(lenderId.getValue(), amount);

        loanRepository.save(loan);
        lenderRepository.save(lender);
    }


}
