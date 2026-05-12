package com.p2p.domain.lender;
import java.math.BigDecimal;

import com.p2p.domain.valueobject.Money;

/**
 * Domain entity untuk Lender dalam sistem P2P Lending.
 * Mengelola saldo, status KYC, dan operasi finansial lender.
 */
public class Lender {
    private static final BigDecimal MINIMAL_WITHDRAWAL = new BigDecimal("100000");
    
    private String id;
    private Money saldoBalance;
    private boolean kycStatus;

    public Lender(String id, Money saldoAwal) {
        this.id = id;
        this.saldoBalance = saldoAwal;
        this.kycStatus = false;
    }

    /**
     * Mengurangi saldo lender untuk investasi pinjaman.
     * @param nominalInvestasi nominal yang akan diinvestasikan
     * @throws IllegalArgumentException jika saldo tidak mencukupi
     */
    public void kurangiSaldoUntukInvestasi(Money nominalInvestasi) {
        this.validateInvestmentBalance(nominalInvestasi);
        this.updateBalance(nominalInvestasi.getAmount().negate());
    }

    private void validateInvestmentBalance(Money nominalInvestasi) {
        if (this.saldoBalance.getAmount().compareTo(nominalInvestasi.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo tidak mencukupi untuk melakukan investasi.");
        }
    }

    /**
     * Menambah saldo lender (deposit/top-up).
     * @param nominalTambah nominal yang akan ditambahkan
     * @throws IllegalArgumentException jika nominal tidak valid
     */
    public void tambahSaldo(Money nominalTambah) {
        this.validatePositiveAmount(nominalTambah);
        this.updateBalance(nominalTambah.getAmount());
    }

    private void validatePositiveAmount(Money nominalTambah) {
        if (nominalTambah.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal tambahan harus lebih dari 0");
        }
    }

    /**
     * Menarik saldo lender (withdrawal).
     * Melakukan validasi KYC, minimal withdrawal, dan saldo cukup.
     * @param nominalTarik nominal yang akan ditarik
     * @throws IllegalStateException jika KYC belum terverifikasi
     * @throws IllegalArgumentException jika nominal tidak memenuhi syarat
     */
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

    /**
     * Update saldo dengan amount (positif untuk increment, negatif untuk decrement).
     * @param amount perubahan nominal saldo
     */
    private void updateBalance(BigDecimal amount) {
        BigDecimal newBalance = this.saldoBalance.getAmount().add(amount);
        this.saldoBalance = new Money(newBalance, this.saldoBalance.getCurrency());
    }

    /**
     * Set status KYC lender.
     * @param status true jika sudah terverifikasi, false sebaliknya
     */
    /**
     * Set status KYC lender.
     * @param status true jika sudah terverifikasi, false sebaliknya
     */
    public void setKycStatus(boolean status) {
        this.kycStatus = status;
    }

    /**
     * Cek apakah lender sudah terverifikasi KYC.
     * @return true jika sudah terverifikasi, false sebaliknya
     */
    public boolean isKycVerified() {
        return this.kycStatus;
    }

    /**
     * Dapatkan saldo terkini lender.
     * @return Money object berisi saldo dan currency
     */
    public Money getSaldoBalance() {
        return this.saldoBalance;
    }

    /**
     * Dapatkan ID lender.
     * @return ID lender
     */
    public String getId() {
        return id;
    }
}
