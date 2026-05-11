package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;

public class LoanService {
    private LoanRepository loanRepository;
    private BorrowerRepository borrowerRepository;
    private NotificationService notificationService;

    public LoanService() {} // Default constructor if needed by other tests
    
    public LoanService(LoanRepository loanRepository, BorrowerRepository borrowerRepository) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
    }

    public LoanService(BorrowerRepository borrowerRepository, LoanRepository loanRepository, NotificationService notificationService) {
        this.borrowerRepository = borrowerRepository;
        this.loanRepository = loanRepository;
        this.notificationService = notificationService;
    }

    public Loan ajukanPinjaman(String borrowerId, Money amount) throws Exception {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new Exception("Borrower tidak ditemukan"));

        if (!borrower.isKycStatus()) {
            throw new Exception("Borrower belum terverifikasi KYC");
        }

        if (amount.getAmount().compareTo(borrower.getLimitPinjaman().getAmount()) > 0) {
            throw new Exception("Nominal pinjaman melebihi limit peminjaman");
        }

        if (borrower.getCreditScore() > 0 && borrower.getCreditScore() < 600) {
            throw new Exception("Credit score di bawah ambang batas");
        }

        Loan loan = new Loan("LN-NEW", borrowerId, amount, 12);
        loanRepository.save(loan);
        return loan;
    }

    public void bayarCicilan(String loanId, Money amount) throws Exception {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new Exception("Loan tidak ditemukan");
        loan.payInstallment(amount);
        loanRepository.save(loan);
    }

    public void prosesPencairan(String loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan tidak ditemukan");
        }

        if (loan.getStatus().equals("FUNDING_READY")) {
            loan.ubahStatus("DISBURSED");
            loanRepository.save(loan);
            return;
        }

        if (loan.getStatus().equals("FUNDING")) {
            Money terkumpul = loan.getTotalTerkumpul();
            Money target = loan.getTargetNominal();
            if (terkumpul.getAmount().compareTo(target.getAmount()) < 0) {
                throw new IllegalStateException("Pencairan ditolak, pendanaan belum terpenuhi");
            }
            loan.ubahStatus("FUNDING_READY");
            loanRepository.save(loan);
            return;
        }

        throw new IllegalStateException("Status loan tidak valid untuk pencairan");
    }

    public String kirimNotifikasiPencairan(String loanId) {
    Loan loan = loanRepository.findById(loanId);
    if (loan == null) {
        throw new IllegalArgumentException("Loan tidak ditemukan");
    }

    String borrowerId = loan.getBorrowerId();

    if (loan.getStatus().equals("DISBURSED")) {
        notificationService.kirimNotifikasi(borrowerId, "Dana berhasil dicairkan");
        return "berhasil";
    }

    notificationService.kirimNotifikasi(borrowerId, "Pencairan gagal: pendanaan belum terpenuhi");
    return "gagal";
}
}
