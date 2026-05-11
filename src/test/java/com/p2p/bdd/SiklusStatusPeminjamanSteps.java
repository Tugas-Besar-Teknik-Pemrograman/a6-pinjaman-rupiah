package com.p2p.bdd;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SiklusStatusPeminjamanSteps {
    private Loan loan;
    private String rejectionReason;

    @Given("borrower memiliki akun yang terverifikasi")
    public void borrower_memiliki_akun_yang_terverifikasi() {
        Borrower borrower = new Borrower("borrower1", new Money(new BigDecimal("1000000"), "IDR"));
        borrower.setKycStatus(true);
    }

    @When("borrower mengajukan pinjaman dengan jumlah tertentu")
    public void borrower_mengajukan_pinjaman_dengan_jumlah_tertentu() {
        loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
        loan.ubahStatus("FUNDING");
    }

    @Then("status peminjaman harus berubah menjadi {string}")
    public void status_peminjaman_harus_berubah_menjadi(String expectedStatus) {
        assertEquals(expectedStatus, loan.getStatus());
    }

    @Given("borrower telah mengajukan pinjaman dan statusnya {string}")
    public void borrower_telah_mengajukan_pinjaman_dan_statusnya(String status) {
        loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
        loan.ubahStatus(status);
    }

    @When("Lender mendanai pinjaman tersebut")
    public void lender_mendanai_pinjaman_tersebut() {
        loan.ubahStatus("FUNDING_READY");
    }

    @Given("borrower telah menerima dana pinjaman")
    public void borrower_telah_menerima_dana_pinjaman() {
        loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
        loan.ubahStatus("FUNDING_READY");
    }

    @When("proses pencairan selesai")
    public void proses_pencairan_selesai() {
        loan.ubahStatus("DISBURSED");
    }

    @Given("borrower memiliki pinjaman aktif")
    public void borrower_memiliki_pinjaman_aktif() {
        loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
        loan.ubahStatus("REPAYMENT");
    }

    @When("borrower melakukan pembayaran cicilan")
    public void borrower_melakukan_pembayaran_cicilan() {
        loan.ubahStatus("REPAYMENT");
    }

    @Then("status masih {string} sampai semua cicilan lunas")
    public void status_masih_sampai_semua_cicilan_lunas(String status) {
        assertEquals(status, loan.getStatus());
    }

    @Given("borrower masih memiliki pinjaman terakhir yang harus di bayar")
    public void borrower_masih_memiliki_pinjaman_terakhir_yang_harus_di_bayar() {
        loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
        loan.ubahStatus("REPAYMENT");
    }

    @When("borrower melakukan pembayaran cicilan terakhir")
    public void borrower_melakukan_pembayaran_cicilan_terakhir() {
        loan.ubahStatus("CLOSED");
    }

    @Then("status berubah menjadi {string} karena sudah lunas")
    public void status_berubah_menjadi_karena_sudah_lunas(String status) {
        assertEquals(status, loan.getStatus());
    }

    @Given("borrower mengajukan pinjaman")
    public void borrower_mengajukan_pinjaman() {
        loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
        loan.ubahStatus("FUNDING");
    }

    @When("sistem melakukan validasi pinjaman")
    public void sistem_melakukan_validasi_pinjaman() {
        rejectionReason = "ditolak oleh validasi";
        loan.ubahStatus("REJECTED");
    }

    @Then("status pengajuan berubah menjadi {string}")
    public void status_pengajuan_berubah_menjadi(String status) {
        assertEquals(status, loan.getStatus());
    }

    @Then("Diberikan text penolakan yang berisi alasan kenapa ditolak")
    public void diberikan_text_penolakan_yang_berisi_alasan_kenapa_ditolak() {
        assertNotNull(rejectionReason);
    }

    @When("tidak ada lender yang mendanai dalam waktu tertentu")
    public void tidak_ada_lender_yang_mendanai_dalam_waktu_tertentu() {
        loan.ubahStatus("CANCELLED");
    }

    @When("borrower melewati tanggal jatuh tempo pembayaran cicilan")
    public void borrower_melewati_tanggal_jatuh_tempo_pembayaran_cicilan() {
        loan.ubahStatus("OVERDUE");
    }

    @Then("status peminjaman berubah menjadi {string}")
    public void status_peminjaman_berubah_menjadi(String status) {
        assertEquals(status, loan.getStatus());
    }

    @Given("borrower memiliki pinjaman dengan status {string}")
    public void borrower_memiliki_pinjaman_dengan_status(String status) {
        loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
        loan.ubahStatus(status);
    }

    @When("borrower melakukan pembayaran cicilan setelah jatuh tempo dengan dendanya")
    public void borrower_melakukan_pembayaran_cicilan_setelah_jatuh_tempo_dengan_dendanya() {
        loan.ubahStatus("REPAYMENT");
    }

    @Then("status peminjaman berubah kembali menjadi {string}")
    public void status_peminjaman_berubah_kembali_menjadi(String status) {
        assertEquals(status, loan.getStatus());
    }
}
