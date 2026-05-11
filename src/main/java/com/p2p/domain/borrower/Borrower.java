package com.p2p.domain.borrower;

import com.p2p.domain.valueobject.Money;

public class Borrower {

    private String id;
    private Money limitPinjaman;
    private boolean kycStatus;
    private int creditScore;

    public Borrower(String id, Money limitAwal) {
        this.id = id;
        this.limitPinjaman = limitAwal;
        this.kycStatus = false;
        this.creditScore = 0;
    }

    public void BandingkanLimit(Money nominalPinjaman) {
        if (nominalPinjaman.getAmount().compareTo(this.limitPinjaman.getAmount()) > 0) {
            throw new IllegalArgumentException("Nominal melebihi limit!");
        }
        this.limitPinjaman = new Money(
                this.limitPinjaman.getAmount().subtract(nominalPinjaman.getAmount()), "IDR");
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

    public int getCreditScore() {
        return creditScore;
    }
}