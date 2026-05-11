package com.p2p.domain.lender;
import java.math.BigDecimal;

import com.p2p.domain.valueobject.Money;

public class Lender {
    private String id;
    private Money saldoBalance;

    public Lender(String id, Money saldoAwal) {
        this.id = id;
        this.saldoBalance = saldoAwal;

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

    public String getId() { return id; }

}