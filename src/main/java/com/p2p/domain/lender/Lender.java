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
        
    // 1. Ambil nilai BigDecimal dari objek Money
    BigDecimal amountToInvest = nominalInvestasi.getAmount();
    BigDecimal currentBalance = this.saldoBalance.getAmount();

    // 2. Validasi apakah saldo cukup (currentBalance < amountToInvest)
    // compareTo mengembalikan -1 jika lebih kecil, 0 jika sama, 1 jika lebih besar
    if (currentBalance.compareTo(amountToInvest) < 0) {
        throw new IllegalArgumentException("Saldo tidak mencukupi untuk melakukan investasi.");
    }

    // 3. Update saldo dengan membuat objek Money baru (Immutability)
    // Mengurangi saldo saat ini dengan nominal investasi
    BigDecimal newBalanceAmount = currentBalance.subtract(amountToInvest);
    
    this.saldoBalance = new Money(newBalanceAmount, this.saldoBalance.getCurrency());
}

    public void tambahSaldo(Money nominalTambah) throws Exception {
        // 1. Validasi nominal harus positif
        if (nominalTambah.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Nominal tambahan harus lebih dari 0");
        }

        // 2. Tambah saldo
        BigDecimal newBalance = this.saldoBalance.getAmount().add(nominalTambah.getAmount());
        this.saldoBalance = new Money(newBalance, this.saldoBalance.getCurrency());
    }

    public void tarikSaldo(Money nominalTarik) throws Exception {
        // 1. Validasi KYC status
        if (!this.kycStatus) {
            throw new Exception("Lender tidak terverifikasi (KYC = false)");
        }

        // 2. Validasi minimal penarikan 100k
        BigDecimal minimalWithdrawal = new BigDecimal("100000");
        if (nominalTarik.getAmount().compareTo(minimalWithdrawal) < 0) {
            throw new Exception("Nominal penarikan minimal harus 100000");
        }

        // 3. Validasi saldo cukup
        if (this.saldoBalance.getAmount().compareTo(nominalTarik.getAmount()) < 0) {
            throw new Exception("Saldo tidak mencukupi untuk melakukan penarikan");
        }

        // 4. Kurangi saldo
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
