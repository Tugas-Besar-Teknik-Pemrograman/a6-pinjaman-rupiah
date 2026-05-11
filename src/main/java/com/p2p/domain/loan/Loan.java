package com.p2p.domain.loan;

import com.p2p.domain.valueobject.Money;
import com.p2p.domain.loan.strategy.InterestCalculationStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Loan {
    private String id;
    private String borrowerId;
    private Money targetNominal;
    private Money totalTerkumpul;
    private Money remainingPrincipal;
    private int tenor;
    private String status;
    
    private InterestCalculationStrategy interestStrategy;
    private Money currentMonthBill;

    public Loan(String id, String borrowerId, Money targetNominal, int tenor) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.targetNominal = targetNominal;
        this.remainingPrincipal = targetNominal;
        this.tenor = tenor;
        this.status = "PROPOSED";
        this.currentMonthBill = new Money(BigDecimal.ZERO, "IDR");
    }

    public void setInterestStrategy(InterestCalculationStrategy strategy) {
        this.interestStrategy = strategy;
    }

    public void tambahPendanaan(String lenderId, Money investasiDiberikan) {
        // Jumlahkan uang yang sudah ada dengan investasi yang baru masuk
        java.math.BigDecimal totalBaru = this.totalTerkumpul.getAmount().add(investasiDiberikan.getAmount());
        
        // Simpan uang barunya ke dalam variabel totalTerkumpul
        this.totalTerkumpul = new Money(totalBaru, this.totalTerkumpul.getCurrency());
      
    public void generateMonthlyBill() {
        if (this.interestStrategy == null) throw new IllegalStateException("Strategy not set");
        this.currentMonthBill = this.interestStrategy.calculateInstallment(targetNominal, remainingPrincipal, tenor);
    }

    public void payInstallment(Money paymentAmount) {
        if (!this.status.equals("DISBURSED")) {
            throw new IllegalStateException("Pinjaman belum masuk masa pembayaran");
        }
        
        if (paymentAmount.getAmount().compareTo(this.currentMonthBill.getAmount()) < 0) {
            throw new IllegalArgumentException("Nominal pembayaran kurang dari nominal tagihan");
        }

        // Update sisa pokok 
        BigDecimal installmentBase = targetNominal.getAmount().divide(new BigDecimal(tenor), 2, RoundingMode.HALF_UP);
        this.remainingPrincipal = new Money(this.remainingPrincipal.getAmount().subtract(installmentBase), "IDR");
        
        // Reset tagihan bulan ini karena sudah lunas
        this.currentMonthBill = new Money(BigDecimal.ZERO, "IDR");
    }

    public void ubahStatus(String status) { 
        this.status = status; 
    }

    public void setTotalTerkumpul(Money totalTerkumpul) {
        this.totalTerkumpul = totalTerkumpul;
    }

    public Money getTotalTerkumpul() {
        return totalTerkumpul;
    }

    public Money getTargetNominal() {
        return targetNominal;
    }

    public boolean isLayakNotifikasiPencairan() {
    return this.status.equals("DISBURSED");
}

    public String getId() { return id; }
    public String getBorrowerId() { return borrowerId; }
    public String getStatus() { return status; }
    public Money getCurrentMonthBill() { 
        return currentMonthBill; 
    }

    public String getId() { 
        return id; 
    }
}