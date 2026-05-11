package com.p2p.domain.borrower;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;

public class Borrower {

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
    }

    public Loan ajukanPinjaman(String loanid,Money nominal, int tenor){

        //validasi KYC
        if(this.kycStatus == false){
            throw new IllegalStateException("Peminjaman ditolak karena Borrower belum terverifikasi (KYC)");
        }

        //validasi credit score
        if(this.creditScore < 600){
            throw new IllegalStateException("Peminjaman ditolak karena Credit score di bawah ambang batas");
        }

        //validasi nominal gaboleh <= 0
        if (nominal.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal pinjaman harus lebih dari 0");
        }
        //validasi nominal <= limit
        if (this.limitPinjaman.isLessThan(nominal)) {
            // Jika sisa limit lebih kecil dari yang mau dipinjam, tolak!
            throw new IllegalStateException("Sisa limit pinjaman tidak mencukupi");
        }
        //validasi pinjaman aktif
        if (this.hasActiveLoan) {
            throw new IllegalStateException("Harap lunasi pinjaman sebelumnya terlebih dahulu");
        }

        //kurangin limit
        this.limitPinjaman = this.limitPinjaman.subtract(nominal);

        Loan loan = new Loan(loanid, this.id, nominal, tenor);
        loan.ubahStatus("FUNDING");
        return loan;
    }

    public void BandingkanLimit(Money nominalPinjaman) {
        if (nominalPinjaman.getAmount().compareTo(this.limitPinjaman.getAmount()) > 0) {
            throw new IllegalArgumentException("Nominal melebihi limit!");
        }
        this.limitPinjaman = new Money(
                this.limitPinjaman.getAmount().subtract(nominalPinjaman.getAmount()), "IDR");
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

    public void setLimitPinjaman(Money limit) {
        this.limitPinjaman = limit;
    }
}