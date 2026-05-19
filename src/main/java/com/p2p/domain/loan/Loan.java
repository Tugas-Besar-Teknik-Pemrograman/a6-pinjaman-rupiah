package com.p2p.domain.loan;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.strategy.InterestCalculationStrategy;
import com.p2p.domain.state.LoanStateFactory;
import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class Loan {
    private LoanId loanid;
    private BorrowerId borrowerId;
    private Money targetNominal;
    private Money totalTerkumpul;
    private Money remainingPrincipal;
    private int tenor;
    private int tenorSisa;
    private String status;

    private InterestCalculationStrategy interestStrategy;
    private Money currentMonthBill;
    private Map<LenderId, Money> daftarPendana;

    public Loan(LoanId loanid, BorrowerId borrowerId, Money targetNominal, int tenor) {
        this.loanid = loanid;
        this.borrowerId = borrowerId;
        this.targetNominal = targetNominal;
        this.tenor = tenor;
        this.tenorSisa = tenor;
        this.remainingPrincipal = targetNominal;
        this.totalTerkumpul = new Money(BigDecimal.ZERO, "IDR");
        this.daftarPendana = new HashMap<>();
        this.status = "PENDING";
        this.currentMonthBill = new Money(BigDecimal.ZERO, "IDR");
    }

    public Loan(LoanId loanid, BorrowerId borrowerId, Money targetNominal) {
        this(loanid, borrowerId, targetNominal, 12);
    }

    public void ubahStatus(String statusBaru) {
        this.status = statusBaru;
    }

    public void tambahPendanaan(LenderId lenderId, Money investasiDiberikan) {
        BigDecimal totalBaru = this.totalTerkumpul.getAmount().add(investasiDiberikan.getAmount());

        if (totalBaru.compareTo(this.targetNominal.getAmount()) > 0) {
            throw new IllegalArgumentException("Nominal investasi melebihi target pendanaan");
        }

        this.totalTerkumpul = new Money(totalBaru, this.totalTerkumpul.getCurrency());

        if (this.daftarPendana.containsKey(lenderId)) {
            BigDecimal uangLama = this.daftarPendana.get(lenderId).getAmount();
            BigDecimal akumulasi = uangLama.add(investasiDiberikan.getAmount());
            this.daftarPendana.put(lenderId, new Money(akumulasi, investasiDiberikan.getCurrency()));
        } else {
            this.daftarPendana.put(lenderId, investasiDiberikan);
        }
        
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

    // MODIFIKASI: Penambahan denda OVERDUE
    public void generateMonthlyBill() {
        if (this.interestStrategy != null) {
            Money tagihanNormal = this.interestStrategy.calculateInstallment(
                    this.targetNominal, this.remainingPrincipal, this.tenor);
            
            BigDecimal totalAmount = tagihanNormal.getAmount();

            if ("OVERDUE".equals(this.status)) {
                BigDecimal dendaOverdue = new BigDecimal("50000"); // Contoh denda flat 50.000
                totalAmount = totalAmount.add(dendaOverdue);
            }

            this.currentMonthBill = new Money(totalAmount, "IDR");
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
        } else if ("OVERDUE".equals(this.status)) {
            // Jika sebelumnya OVERDUE lalu bayar lunas, kembalikan ke REPAYMENT
            this.ubahStatus("REPAYMENT"); 
        }

        if (isLunas()) {
            LoanStateFactory.closed().ubahStatus(this);
        }
    }

    public Money getSisaTagihanKeseluruhan() {
        if (this.remainingPrincipal == null) {
            return this.targetNominal;
        }
        return this.remainingPrincipal;
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

    public LoanId getId() {
        return loanid;
    }

    public BorrowerId getBorrowerId() {
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