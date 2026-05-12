package com.p2p.application.service;

import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;

public class FundingService {

    private final LoanRepository loanRepository;
    private final LenderRepository lenderRepository;

    public FundingService(LoanRepository loanRepository, LenderRepository lenderRepository) {
        this.loanRepository = loanRepository;
        this.lenderRepository = lenderRepository;
    }

    public void invest(String lenderId, String loanId, Money amount) throws Exception {
        if (amount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new Exception("Nominal investasi harus lebih dari 0");
        }

        Loan loan = loanRepository.findById(loanId);
        
        if (!"FUNDING".equals(loan.getStatus())) {
            throw new Exception("Investasi ditolak, status Loan bukan FUNDING");
        }
        loan.tambahPendanaan(lenderId, amount);

        loanRepository.save(loan);
    }


}
