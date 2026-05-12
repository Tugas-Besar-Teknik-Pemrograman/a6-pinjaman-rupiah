package com.p2p.domain.lender;
import java.math.BigDecimal;

import com.p2p.domain.valueobject.Money;

public class Lender {
    private String id;
    private Money saldoBalance;
    private boolean kycStatus;

    public Lender(String id, Money saldoAwal) {
        this.id = id;
        this.saldoBalance = saldoAwal;
        this.kycStatus = false;
    }

    public void kurangiSaldoUntukInvestasi(Money nominalInvestasi) {
        this.validateInvestmentBalance(nominalInvestasi);
        this.subtractBalance(nominalInvestasi);
    }

    private void validateInvestmentBalance(Money nominalInvestasi) {
        BigDecimal currentBalance = this.saldoBalance.getAmount();
        BigDecimal investmentAmount = nominalInvestasi.getAmount();

        if (currentBalance.compareTo(investmentAmount) < 0) {
            throw new IllegalArgumentException("Saldo tidak mencukupi untuk melakukan investasi.");
        }
    }

    private void subtractBalance(Money nominalInvestasi) {
        BigDecimal newBalance = this.saldoBalance.getAmount()
            .subtract(nominalInvestasi.getAmount());
        this.saldoBalance = new Money(newBalance, this.saldoBalance.getCurrency());
    }

    public void tambahSaldo(Money nominalTambah) throws Exception {
        this.validatePositiveAmount(nominalTambah);
        this.addBalance(nominalTambah);
    }

    private void validatePositiveAmount(Money nominalTambah) throws Exception {
        if (nominalTambah.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Nominal tambahan harus lebih dari 0");
        }
    }

    private void addBalance(Money nominalTambah) {
        BigDecimal newBalance = this.saldoBalance.getAmount().add(nominalTambah.getAmount());
        this.saldoBalance = new Money(newBalance, this.saldoBalance.getCurrency());
    }

    public void tarikSaldo(Money nominalTarik) throws Exception {
        this.validateKycStatus();
        this.validateMinimalWithdrawal(nominalTarik);
        this.validateSufficientBalance(nominalTarik);
        this.decreaseBalance(nominalTarik);
    }

    private void validateKycStatus() throws Exception {
        if (!this.kycStatus) {
            throw new Exception("Lender tidak terverifikasi (KYC = false)");
        }
    }

    private void validateMinimalWithdrawal(Money nominalTarik) throws Exception {
        BigDecimal minimalWithdrawal = new BigDecimal("100000");
        if (nominalTarik.getAmount().compareTo(minimalWithdrawal) < 0) {
            throw new Exception("Nominal penarikan minimal harus 100000");
        }
    }

    private void validateSufficientBalance(Money nominalTarik) throws Exception {
        if (this.saldoBalance.getAmount().compareTo(nominalTarik.getAmount()) < 0) {
            throw new Exception("Saldo tidak mencukupi untuk melakukan penarikan");
        }
    }

    private void decreaseBalance(Money nominalTarik) {
        BigDecimal newBalance = this.saldoBalance.getAmount().subtract(nominalTarik.getAmount());
        this.saldoBalance = new Money(newBalance, this.saldoBalance.getCurrency());
    }

    public void setKycStatus(boolean status) {
        this.kycStatus = status;
    }

    public boolean isKycVerified() {
        return this.kycStatus;
    }

    public Money getSaldoBalance() {
        return this.saldoBalance;
    }

    public String getId() { return id; }
}
