package com.p2p.application.service;

import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;

public class FundingService {

    private final LoanRepository loanRepository;
    private final LenderRepository lenderRepository;

     // Constructor untuk memasukkan repository (Dependency Injection)
    public FundingService(LoanRepository loanRepository, LenderRepository lenderRepository) {
        this.loanRepository = loanRepository;
        this.lenderRepository = lenderRepository;
    }

    // Fungsi utama yang akan dites
    public void invest(String lenderId, String loanId, Money amount) throws Exception {
        
    }
}
