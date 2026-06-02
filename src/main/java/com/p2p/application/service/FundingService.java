package com.p2p.application.service;

import com.p2p.application.observer.LoanEventPublisher;
import com.p2p.domain.event.InvestasiDiterimaEvent;
import com.p2p.domain.event.PendanaanTerpenuhiEvent;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanStatus;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;

public class FundingService {

    private final LoanRepository loanRepository;
    private final LenderRepository lenderRepository;
    private final LoanEventPublisher publisher;

    public FundingService(LoanRepository loanRepository, LenderRepository lenderRepository, LoanEventPublisher publisher) {
        this.loanRepository = loanRepository;
        this.lenderRepository = lenderRepository;
        this.publisher = publisher;
    }

    private static final java.math.BigDecimal MINIMAL_INVESTASI = new java.math.BigDecimal("100000");

    public void invest(LenderId lenderId, LoanId loanId, Money amount) {
        if (amount.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal investasi harus lebih dari 0");
        }
        if (amount.getAmount().compareTo(MINIMAL_INVESTASI) < 0) {
            throw new IllegalArgumentException("Nominal investasi minimal Rp 100.000");
        }
        if (amount.getAmount().remainder(MINIMAL_INVESTASI).compareTo(java.math.BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Nominal investasi harus kelipatan Rp 100.000");
        }

        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Pinjaman tidak ditemukan");

        if (loan.getStatusEnum() != LoanStatus.FUNDING) {
            throw new IllegalStateException("Investasi ditolak, status Loan bukan FUNDING");
        }

        // Validasi sisa target SEBELUM memotong saldo — agar saldo tidak hilang jika investasi gagal
        java.math.BigDecimal sisaTarget = loan.getTargetNominal().getAmount()
                .subtract(loan.getTotalTerkumpul().getAmount());
        if (amount.getAmount().compareTo(sisaTarget) > 0) {
            throw new IllegalArgumentException("Nominal investasi melebihi target pendanaan");
        }

        Lender lender = lenderRepository.findById(lenderId);
        if (lender == null) throw new IllegalArgumentException("Lender tidak ditemukan");

        lender.kurangiSaldoUntukInvestasi(amount);
        loan.tambahPendanaan(lenderId, amount);

        loanRepository.save(loan);
        lenderRepository.save(lender);

        // Fire event investasi diterima → masuk kotak notifikasi lender
        publisher.publishInvestasiDiterima(new InvestasiDiterimaEvent(loanId, lenderId));

        // Jika pendanaan sudah terpenuhi 100% → fire event ke borrower & lender
        if (loan.getStatusEnum() == LoanStatus.FUNDING_READY) {
            publisher.publishPendanaanTerpenuhi(new PendanaanTerpenuhiEvent(loanId, loan.getBorrowerId()));
        }
    }
}