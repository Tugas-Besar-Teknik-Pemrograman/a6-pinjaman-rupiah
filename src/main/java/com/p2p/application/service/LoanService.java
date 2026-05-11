package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;

public class LoanService {
    private final LoanRepository loanRepository;
    
    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public void bayarCicilan(String loanId, Money amount) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new RuntimeException("Loan not found");
        
        loan.payInstallment(amount);
    }
}
