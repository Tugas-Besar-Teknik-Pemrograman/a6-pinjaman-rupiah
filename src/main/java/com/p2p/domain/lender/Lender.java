package com.p2p.domain.lender;
import com.p2p.domain.valueobject.Money;

public class Lender {
    private String id;
    private Money saldoBalance;

    public Lender(String id, Money saldoAwal) {
        this.id = id;
        this.saldoBalance = saldoAwal;

    }

    public void kurangiSaldoUntukInvestasi(Money nominalInvestasi) {
        // Logikanya menyusul
    }
    public String getId() { return id; }

}