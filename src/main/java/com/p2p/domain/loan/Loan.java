package com.p2p.domain.loan;
import com.p2p.domain.valueobject.Money;

public class Loan {
    private String id;
    private String borrowerId;
    private Money targetNominal;
    private Money totalTerkumpul;

    private String status;

    // private List<Repayment> daftarCicilan = new ArrayList<>();

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
        // Jumlahkan uang yang sudah ada dengan investasi yang baru masuk
        java.math.BigDecimal totalBaru = this.totalTerkumpul.getAmount().add(investasiDiberikan.getAmount());
        
        // Simpan uang barunya ke dalam variabel totalTerkumpul
        this.totalTerkumpul = new Money(totalBaru, this.totalTerkumpul.getCurrency());
    }

    public void bayarCicilan(String repaymentId, Money jumlahBayar) {
    }

    public String getId() { return id; }
    public String getStatus() { return status; }

    public Money getTotalTerkumpul() {
        return totalTerkumpul;
    }
}