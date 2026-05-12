package com.p2p.domain.borrower;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;

public class Borrower {
    private static final int AMBANG_BATAS = 600;

    private String id;
    private Money limitPinjaman;
    private boolean kycStatus;
    private int creditScore;
    private boolean hasActiveLoan;

    public Borrower(String id, Money limit) {
        this.id = id;
        this.limitPinjaman = limit;
        this.kycStatus = false;
        this.creditScore = 0;
        this.hasActiveLoan = false;
    }

    public Loan ajukanPinjaman(String loanid,Money nominal, int tenor){
        validasiPinjaman(nominal);
        this.hasActiveLoan = true;
        Loan loan = new Loan(loanid, this.id, nominal, tenor);
        loan.ubahStatus("FUNDING");
        return loan;
    }

    private void validasiPinjaman(Money nominal){
        //validasi KYC
        if(!this.kycStatus){
            throw new IllegalStateException("Peminjaman ditolak karena Borrower belum terverifikasi (KYC)");
        }

        //validasi credit score
        if(this.creditScore < AMBANG_BATAS){
            throw new IllegalStateException("Peminjaman ditolak karena Credit score di bawah ambang batas");
        }

        //validasi nominal gaboleh <= 0
        if (nominal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal pinjaman harus lebih dari 0");
        }

        //validasi nominal <= limit
        if (this.limitPinjaman.isLessThan(nominal)) {
            // Jika sisa limit lebih kecil dari yang mau dipinjam, tolak!
            throw new IllegalStateException("Sisa limit pinjaman tidak mencukupi");
        }

        //validasi pinjaman aktif
        if (this.hasActiveLoan) {
            throw new IllegalStateException("Lunasi Peminjaman sebelumnya dulu");
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Money getLimitPinjaman() {
        return limitPinjaman;
    }
    public void setLimitPinjaman(Money limit) {
        this.limitPinjaman = limit;
    }

    public void setKycStatus(boolean status) {
        this.kycStatus = status;
    }
    public boolean isKycStatus() {
        return kycStatus;
    }

    public void setCreditScore(int score) {
        this.creditScore = score;
    }

    public void setHasActiveLoan(boolean hasActiveLoan) {
        this.hasActiveLoan = hasActiveLoan;
    }
    public boolean hasActiveLoan() {
        return hasActiveLoan;
    }
}