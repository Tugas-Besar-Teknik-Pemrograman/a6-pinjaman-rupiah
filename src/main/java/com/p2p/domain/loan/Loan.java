package com.p2p.domain.loan;
import com.p2p.domain.valueobject.Money;
import java.util.ArrayList;
import java.util.List;

public class Loan {
    // --- ATRIBUT DASAR ---
    private String id;
    private String borrowerId;
    private Money targetNominal;
    private Money totalTerkumpul;

    private String status;

    // private List<Repayment> daftarCicilan = new ArrayList<>();

    // --- CONSTRUCTOR ---
    public Loan(String id, String borrowerId, Money targetNominal) {
        this.id = id;
        this.borrowerId = borrowerId;
        this.targetNominal = targetNominal;
        this.totalTerkumpul = new Money(new java.math.BigDecimal("0"), "IDR");
        this.status = "PROPOSED";
    }

    // --- METHOD KERANGKA UNTUK TDD MASING-MASING ---

    public void ubahStatus(String statusBaru) {
        // Jangan diisi dulu, biarkan yang isi saat ngerjain TDD-nya
    }

    public void tambahPendanaan(String lenderId, Money investasiDiberikan) {
        // Tambahkan nilai ke totalTerkumpul
        // Bikin if-condition, kalau totalTerkumpul == targetNominal, trigger Observer (Event)
    }

    public void bayarCicilan(String repaymentId, Money jumlahBayar) {
        //  yang akan isi logikanya nanti
    }

    // Getter...
    public String getId() { return id; }
    public String getStatus() { return status; }

}