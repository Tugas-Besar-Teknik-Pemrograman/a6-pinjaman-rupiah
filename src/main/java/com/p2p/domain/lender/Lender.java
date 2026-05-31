package com.p2p.domain.lender;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.p2p.domain.valueobject.Money;

public class Lender {
    private static final BigDecimal MINIMAL_WITHDRAWAL = new BigDecimal("100000");
    
    private LenderId id;
    private Money saldoBalance;
    private boolean kycStatus;
    private List<ReturnRecord> riwayatReturn = new ArrayList<>();

    public Lender(LenderId id, Money saldoAwal) {
        this.id = id;
        this.saldoBalance = saldoAwal;
        this.kycStatus = false;
    }
oke
    public void kurangiSaldoUntukInvestasi(Money nominalInvestasi) {
        this.validateInvestmentBalance(nominalInvestasi);
        this.updateBalance(nominalInvestasi.getAmount().negate());
    }

    private void validateInvestmentBalance(Money nominalInvestasi) {
        if (this.saldoBalance.getAmount().compareTo(nominalInvestasi.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo tidak mencukupi untuk melakukan investasi.");
        }
    }

    public void tambahSaldo(Money nominalTambah) {
        this.validatePositiveAmount(nominalTambah);
        this.updateBalance(nominalTambah.getAmount());
    }

    private void validatePositiveAmount(Money nominalTambah) {
        if (nominalTambah.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal tambahan harus lebih dari 0");
        }
    }

    public void tarikSaldo(Money nominalTarik) {
        this.validateKycStatus();
        this.validateMinimalWithdrawal(nominalTarik);
        this.validateSufficientBalance(nominalTarik);
        this.updateBalance(nominalTarik.getAmount().negate());
    }

    private void validateKycStatus() {
        if (!this.kycStatus) {
            throw new IllegalStateException("Lender tidak terverifikasi (KYC = false)");
        }
    }

    private void validateMinimalWithdrawal(Money nominalTarik) {
        if (nominalTarik.getAmount().compareTo(MINIMAL_WITHDRAWAL) < 0) {
            throw new IllegalArgumentException("Nominal penarikan minimal harus " + MINIMAL_WITHDRAWAL);
        }
    }

    private void validateSufficientBalance(Money nominalTarik) {
        if (this.saldoBalance.getAmount().compareTo(nominalTarik.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo tidak mencukupi untuk melakukan penarikan");
        }
    }

    private void updateBalance(BigDecimal amount) {
        BigDecimal newBalance = this.saldoBalance.getAmount().add(amount);
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

    public void tambahReturn(ReturnRecord record) {
        riwayatReturn.add(record);
    }

    public List<ReturnRecord> getRiwayatReturn() {
        return Collections.unmodifiableList(riwayatReturn);
    }

    public LenderId getId() {
        return id;
    }
    public void setId(LenderId id) {
        this.id = id;
    }
}
