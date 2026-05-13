package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.state.LoanStateFactory;
import com.p2p.domain.valueobject.Money;

public class LoanService {
    private LoanRepository loanRepository;
    private BorrowerRepository borrowerRepository;
    private NotificationService notificationService;

    public LoanService() {}
    
    public LoanService(LoanRepository loanRepository, BorrowerRepository borrowerRepository) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
    }

    public LoanService(LoanRepository loanRepository, BorrowerRepository borrowerRepository, NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.notificationService = notificationService;
    }

    public Loan ajukanPinjaman(BorrowerId borrowerId, Money amount, int tenor) throws Exception {
        Borrower borrower = borrowerRepository.findById(borrowerId);
        if (borrower == null) {
            throw new Exception("Borrower tidak ditemukan");
        }

        Loan loan = borrower.ajukanPinjaman(new LoanId(), amount, tenor);

        borrowerRepository.save(borrower);
        loanRepository.save(loan);

        return loan;
    }

    public void bayarCicilan(LoanId loanId, Money amount) throws Exception {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new Exception("Loan tidak ditemukan");
        loan.payInstallment(amount);
        loanRepository.save(loan);
    }

    public void prosesPencairan(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan tidak ditemukan");
        }

        if (loan.getStatus().equals("FUNDING_READY")) {
            LoanStateFactory.disbursed().ubahStatus(loan);
            loanRepository.save(loan);
            return;
        }

        if (loan.getStatus().equals("FUNDING")) {
            Money terkumpul = loan.getTotalTerkumpul();
            Money target = loan.getTargetNominal();
            if (terkumpul.getAmount().compareTo(target.getAmount()) < 0) {
                throw new IllegalStateException("Pencairan ditolak, pendanaan belum terpenuhi");
            }
            LoanStateFactory.fundingReady().ubahStatus(loan);
            loanRepository.save(loan);
            return;
        }

        throw new IllegalStateException("Status loan tidak valid untuk pencairan");
    }

    public String kirimNotifikasiPencairan(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan tidak ditemukan");
        }

        BorrowerId borrowerId = loan.getBorrowerId();

        if (loan.getStatus().equals("DISBURSED")) {
            notificationService.kirimNotifikasi(borrowerId.getValue(), "Dana berhasil dicairkan");
            return "berhasil";
        }

        notificationService.kirimNotifikasi(borrowerId.getValue(), "Pencairan gagal: pendanaan belum terpenuhi");
        return "gagal";
    }
}
