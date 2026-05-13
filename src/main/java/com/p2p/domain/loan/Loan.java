package com.p2p.domain.loan;

import com.p2p.domain.loan.strategy.InterestCalculationStrategy;
import com.p2p.domain.state.LoanStateFactory;
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
    private int tenorSisa;
    private String status;

    private InterestCalculationStrategy interestStrategy;
    private Money currentMonthBill;

    public Loan(String id, String borrowerId, Money targetNominal, int tenor) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.targetNominal = targetNominal;
        this.tenor = tenor;
        this.tenorSisa = tenor;
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

        // Otomatis pindah ke FUNDING_READY jika target terpenuhi
        if (totalBaru.compareTo(this.targetNominal.getAmount()) == 0) {
            LoanStateFactory.fundingReady().ubahStatus(this);
        }
    }

    public void bayarCicilan(String repaymentId, Money jumlahBayar) throws Exception {
        payInstallment(jumlahBayar);
    }

    public void setInterestStrategy(InterestCalculationStrategy strategy) {
        this.interestStrategy = strategy;
    }

    public void generateMonthlyBill() {
        if (this.interestStrategy != null) {
            this.currentMonthBill = this.interestStrategy.calculateInstallment(
                    this.targetNominal, this.remainingPrincipal, this.tenor);
        }
    }

    public void payInstallment(Money paymentAmount) throws Exception {
        if (this.currentMonthBill == null) {
            throw new Exception("Tidak ada tagihan aktif");
        }
        if (paymentAmount.getAmount().compareTo(this.currentMonthBill.getAmount()) < 0) {
            throw new Exception("Nominal pembayaran kurang dari nominal tagihan");
        }

        BigDecimal principalPortion = this.targetNominal.getAmount()
                .divide(new BigDecimal(this.tenor), RoundingMode.HALF_UP);
        this.remainingPrincipal = new Money(
                this.remainingPrincipal.getAmount().subtract(principalPortion),
                this.remainingPrincipal.getCurrency());
        this.currentMonthBill = new Money(BigDecimal.ZERO, this.currentMonthBill.getCurrency());

        this.tenorSisa--;

        if (this.status.equals("DISBURSED")) {
            LoanStateFactory.repayment().ubahStatus(this);
        }

        if (isLunas()) {
            LoanStateFactory.closed().ubahStatus(this);
        }
    }

    public boolean isLunas() {
        return this.tenorSisa <= 0
                || this.remainingPrincipal.getAmount().compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean isPinjamanExpired() {
        return "FUNDING".equals(this.status);
    }

    public boolean isPinjamanOverdue() {
        return "DISBURSED".equals(this.status) || "REPAYMENT".equals(this.status);
    }

    public boolean isOverduePaid() {
        return "OVERDUE".equals(this.status);
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

    public int getTenorSisa() {
        return tenorSisa;
    }

    public Money getCurrentMonthBill() {
        return currentMonthBill;
    }
}