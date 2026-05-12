package com.p2p.domain.loan;

import com.p2p.domain.loan.strategy.InterestCalculationStrategy;
import com.p2p.domain.valueobject.Money;
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
        this.tenor = tenor;
        this.remainingPrincipal = targetNominal;
        this.totalTerkumpul = new Money(BigDecimal.ZERO, "IDR");
        this.status = "FUNDING";
        this.currentMonthBill = new Money(BigDecimal.ZERO, "IDR");
    }

    public Loan(String id, String borrowerId, Money targetNominal) {
        this(id, borrowerId, targetNominal, 12);
    } 

    public void ubahStatus(String statusBaru) {
        this.status = statusBaru;
    }

    public void tambahPendanaan(String lenderId, Money investasiDiberikan) throws Exception {
        BigDecimal totalBaru = this.totalTerkumpul.getAmount().add(investasiDiberikan.getAmount());

        if (totalBaru.compareTo(this.targetNominal.getAmount()) > 0) {
            throw new Exception("Nominal investasi melebihi target pendanaan");
        }

        this.totalTerkumpul = new Money(totalBaru, this.totalTerkumpul.getCurrency());

        // Jika pendanaan sudah mencapai target, ubah status menjadi FUNDING_READY
        if (totalBaru.compareTo(this.targetNominal.getAmount()) == 0) {
            this.status = "FUNDING_READY";
        }
    }

    public void bayarCicilan(String repaymentId, Money jumlahBayar) {
    }

    public void setInterestStrategy(InterestCalculationStrategy strategy) {
        this.interestStrategy = strategy;
    }

    public void generateMonthlyBill() {
        if (this.remainingPrincipal == null) {
            this.remainingPrincipal = this.targetNominal;
        }
        if (this.interestStrategy != null) {
            this.currentMonthBill = this.interestStrategy.calculateInstallment(this.targetNominal, this.remainingPrincipal, this.tenor);
        }
    }

    public void payInstallment(Money paymentAmount) throws Exception {
        if (this.currentMonthBill == null) {
            throw new Exception("Tidak ada tagihan aktif");
        }
        if (paymentAmount.getAmount().compareTo(this.currentMonthBill.getAmount()) < 0) {
            throw new Exception("Nominal pembayaran kurang dari nominal tagihan");
        }

        BigDecimal principalPortion = this.targetNominal.getAmount().divide(new BigDecimal(this.tenor), RoundingMode.HALF_UP);
        this.remainingPrincipal = new Money(this.remainingPrincipal.getAmount().subtract(principalPortion), this.remainingPrincipal.getCurrency());
        this.currentMonthBill = new Money(BigDecimal.ZERO, this.currentMonthBill.getCurrency());
        
        if (this.status.equals("DISBURSED")) {
        this.status = "REPAYMENT";
        }
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

    public String getId() {
        return id;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public String getStatus() {
        return status;
    }

    public Money getCurrentMonthBill() {
        return currentMonthBill;
    }

}
