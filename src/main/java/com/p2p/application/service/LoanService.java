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
    private final NotificationService notificationService;

    public LoanService(BorrowerRepository borrowerRepository, LoanRepository loanRepository, NotificationService notificationService) {
        this.borrowerRepository = borrowerRepository;
        this.loanRepository = loanRepository;
        this.notificationService = notificationService;
    }

    public Loan ajukanPinjaman(String borrowerId, Money nominalPinjaman) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower tidak ditemukan"));

        if (!borrower.isKycStatus()) {
            throw new IllegalStateException("Peminjaman ditolak, KYC belum terverifikasi");
        }

        if (borrower.getCreditScore() < 600) {
            throw new IllegalStateException("Peminjaman ditolak, Credit score di bawah ambang batas");
        }

        if (nominalPinjaman.getAmount().compareTo(borrower.getLimitPinjaman().getAmount()) > 0) {
            throw new IllegalStateException("Peminjaman ditolak, Melebihi limit peminjaman");
        }

        Loan loan = new Loan(UUID.randomUUID().toString(), borrowerId, nominalPinjaman);
        loan.ubahStatus("FUNDING");
        loanRepository.save(loan);
        return loan;
    }

    public void prosesPencairan(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan tidak ditemukan"));

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
    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan tidak ditemukan"));

    String borrowerId = loan.getBorrowerId();

    if (loan.getStatus().equals("DISBURSED")) {
        notificationService.kirimNotifikasi(borrowerId, "Dana berhasil dicairkan");
        return "berhasil";
    }

    notificationService.kirimNotifikasi(borrowerId, "Pencairan gagal: pendanaan belum terpenuhi");
    return "gagal";
}
}