package com.p2p.domain.lender;

import com.p2p.domain.loan.LoanId;
import com.p2p.domain.valueobject.Money;
import java.time.LocalDate;

public class ReturnRecord {
    private final LoanId loanId;
    private final LocalDate tanggal;
    private final Money jumlah;

    public ReturnRecord(LoanId loanId, LocalDate tanggal, Money jumlah) {
        this.loanId = loanId;
        this.tanggal = tanggal;
        this.jumlah = jumlah;
    }

    public LoanId getLoanId() { return loanId; }
    public LocalDate getTanggal() { return tanggal; }
    public Money getJumlah() { return jumlah; }
}
