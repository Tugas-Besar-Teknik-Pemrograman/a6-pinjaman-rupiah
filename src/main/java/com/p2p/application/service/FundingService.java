package com.p2p.application.service;

import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
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

    public void invest(String lenderId, String loanId, Money amount) throws Exception {
        // Minta data Loan dari Repository
        Loan loan = loanRepository.findById(loanId);
        
        if (!"FUNDING".equals(loan.getStatus())) {
            throw new Exception("Investasi ditolak, status Loan bukan FUNDING");
        }

        // Kalau statusnya aman (FUNDING), baru jalankan penambahan dana
        loan.tambahPendanaan(lenderId, amount);
        
        // Simpan perubahan datanya ke repository
        loanRepository.save(loan);
    }

}
