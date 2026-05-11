package com.p2p.domain.borrower;
import com.p2p.domain.valueobject.Money;

public class Borrower {

    private String id;
    private Money limitPinjaman;
    private boolean KYC;
    private int creditScore;

    public Borrower(String id, Money limitAwal) {
        this.id = id;
        this.limitPinjaman = limitAwal;
        this.KYC = false;
        this.creditScore = 0;
    }

    public void kurangiLimit(Money nominalPinjaman) {
    }

    public String getId() {
        return id;
    }
    public Money getLimitPinjaman() {
        return limitPinjaman;
    }

    public void setKycStatus(boolean status) {
        this.KYC = status;
    }
    public boolean isKycStatus() { return KYC; }

    public void setCreditScore(int score) {
        this.creditScore = score;
    }
    public int getCreditScore() {
        return creditScore;
    }

}