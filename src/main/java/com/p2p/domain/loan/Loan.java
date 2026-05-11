package com.p2p.domain.loan;
import com.p2p.domain.valueobject.Money;

public class Loan {
    private String id;
    private String borrowerId;
    private Money targetNominal;
    private Money totalTerkumpul;
    private String status;

    public Loan(String id, String borrowerId, Money targetNominal) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.targetNominal = targetNominal;
        this.totalTerkumpul = new Money(new java.math.BigDecimal("0"), "IDR");
        this.status = "FUNDING";
    }

    public void ubahStatus(String statusBaru) {
        this.status = statusBaru;
    }

    public void tambahPendanaan(String lenderId, Money investasiDiberikan) {
    }

    public void bayarCicilan(String repaymentId, Money jumlahBayar) {
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

    public String getId() { return id; }
    public String getBorrowerId() { return borrowerId; }
    public String getStatus() { return status; }
}