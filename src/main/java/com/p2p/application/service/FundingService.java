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

    // Fungsi utama yang akan dites
    public void invest(String lenderId, String loanId, Money amount) throws Exception {
        // 1. Minta repository mencarikan data Loan berdasarkan ID
        Loan loan = loanRepository.findById(loanId);
        
        // 2. Suruh entitas Loan untuk menambahkan dana investasi
        loan.tambahPendanaan(lenderId, amount);
        
        // 3. Simpan perubahan datanya ke repository
        loanRepository.save(loan);
    }
}
