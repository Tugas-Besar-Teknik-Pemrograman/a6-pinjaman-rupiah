package com.p2p.domain.borrower;
import com.p2p.domain.valueobject.Money;

public class Borrower {

    private String id;
    private Money limitPinjaman; // Pakai Money atau class BorrowerLimit

    public Borrower(String id, Money limitAwal) {
        this.id = id;
        this.limitPinjaman = limitAwal;
    }

    public void kurangiLimit(Money nominalPinjaman) {
        // Logikanya menyusul
    }

    public String getId() { return id; }
    public Money getLimitPinjaman() { return limitPinjaman; }

}