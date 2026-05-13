package com.p2p.infrastructure.memory;

import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.LoanRepository;

public class RepositoryFactory {

    private static RepositoryFactory instance;

    private final LoanRepository loanRepository;
    private final LenderRepository lenderRepository;
    private final BorrowerRepository borrowerRepository;

    private RepositoryFactory() {
        this.loanRepository = new InMemoryLoanRepository();
        this.lenderRepository = new InMemoryLenderRepository();
        this.borrowerRepository = new InMemoryBorrowerRepository();
    }

    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    public LoanRepository getLoanRepository() {
        return loanRepository;
    }

    public LenderRepository getLenderRepository() {
        return lenderRepository;
    }

    public BorrowerRepository getBorrowerRepository() {
        return borrowerRepository;
    }

    public void clearData() {
        ((InMemoryLoanRepository) loanRepository).clear();
        ((InMemoryLenderRepository) lenderRepository).clear();
        ((InMemoryBorrowerRepository) borrowerRepository).clear();
    }
}
