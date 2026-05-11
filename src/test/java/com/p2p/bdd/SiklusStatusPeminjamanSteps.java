package com.p2p.bdd; // Pastikan package-nya sesuai

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import io.cucumber.java.en.*;
import  static org.junit.jupiter.api.Assertions.assertEquals;

    public class SiklusStatusPeminjamanSteps {
        //SSP1: Pengajuan Pinjaman
        @Given("borrower memiliki akun yang terverifikasi")
        public void borrower_memiliki_akun_yang_terverifikasi() {
            Borrower borrower = new Borrower("borrower1", new Money(new BigDecimal("1000000"), "IDR"));
            borrower.setKycStatus(true);
        }
        @When("borrower mengajukan pinjaman dengan jumlah tertentu")
        public void borrower_mengajukan_pinjaman_dengan_jumlah_tertentu() {
            Loan loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
            loan.ubahStatus("FUNDING");
        }
        @Then("status peminjaman harus berubah menjadi {string}")
        public void status_peminjaman_harus_berubah_menjadi(String expectedStatus) {
            Loan loan = new Loan("loan1", "borrower1", new Money(new BigDecimal("500000"), "IDR"));
            loan.ubahStatus("FUNDING");
            assertEquals(expectedStatus, loan.getStatus());
        }

        //SSP2: Pendanaan Pinjaman
        @Given("borrower telah mengajukan pinjaman dan statusnya {string}")
        public void borrower_telah_mengajukan_pinjaman_dan_statusnya(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @When("Lender mendanai pinjaman tersebut")
        public void lender_mendanai_pinjaman_tersebut() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        /**Then("status peminjaman harus berubah menjadi {string}")
        public void status_peminjaman_harus_berubah_menjadi(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        } */ 

        //SSP3: Pencairan Dana
        @Given("borrower telah menerima dana pinjaman")
        public void borrower_telah_menerima_dana_pinjaman() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @When("proses pencairan selesai")
        public void proses_pencairan_selesai() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        /**Then("status peminjaman harus berubah menjadi {string}")
        public void status_peminjaman_harus_berubah_menjadi(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        } */

        //SSP4: Pembayaran Cicilan
        @Given("borrower memiliki pinjaman aktif")
        public void borrower_memiliki_pinjaman_aktif() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @When("borrower melakukan pembayaran cicilan")
        public void borrower_melakukan_pembayaran_cicilan() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @Then("status masih {string} sampai semua cicilan lunas")
        public void status_masih_sampai_semua_cicilan_lunas(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }

        //SSP5: Pelunasan Pinjaman
        @Given("borrower masih memiliki pinjaman terakhir yang harus di bayar")
        public void borrower_masih_memiliki_pinjaman_terakhir_yang_harus_di_bayar() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @When("borrower melakukan pembayaran cicilan terakhir")
        public void borrower_melakukan_pembayaran_cicilan_terakhir() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @Then("status berubah menjadi {string} karena sudah lunas")
        public void status_berubah_menjadi_karena_sudah_lunas(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }

        /*SSP6: Penolakan Pinjaman
        @Given("borrower mengajukan pinjaman")
        public void borrower_mengajukan_pinjaman() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @When("sistem melakukan validasi pinjaman")
        public void sistem_melakukan_validasi_pinjaman() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @Then("status pengajuan berubah menjadi {string}")
        public void status_pengajuan_berubah_menjadi(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @Then("Diberikan text penolakan yang berisi alasan kenapa ditolak")
        public void diberikan_text_penolakan_yang_berisi_alasan_kenapa_ditolak() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        } */

        //SSP7: Pinjaman Kadaluarsa
        @Given("borrower mengajukan pinjaman")
        public void borrower_mengajukan_pinjaman() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @When("tidak ada lender yang mendanai dalam waktu tertentu")
        public void tidak_ada_lender_yang_mendanai_dalam_waktu_tertentu() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @Then("status pengajuan berubah menjadi {string}")
        public void status_pengajuan_berubah_menjadi(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }

        //SSP8: Pinjaman Jatuh Tempo
        /*@Given("borrower memiliki pinjaman aktif")
        public void borrower_memiliki_pinjaman_aktif() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }*/
    
        @When("borrower melewati tanggal jatuh tempo pembayaran cicilan")
        public void borrower_melewati_tanggal_jatuh_tempo_pembayaran_cicilan() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @Then("status peminjaman berubah menjadi {string}")
        public void status_peminjaman_berubah_menjadi(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }

        //SSP9: Pembayaran Cicilan Setelah Jatuh Tempo
        @Given("borrower memiliki pinjaman dengan status {string}")
        public void borrower_memiliki_pinjaman_dengan_status(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @When("borrower melakukan pembayaran cicilan setelah jatuh tempo dengan dendanya")
        public void borrower_melakukan_pembayaran_cicilan_setelah_jatuh_tempo_dengan_dendanya() {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }
        @Then("status peminjaman berubah kembali menjadi {string}")
        public void status_peminjaman_berubah_kembali_menjadi(String string) {
            // Write code here that turns the phrase above into concrete actions
            throw new io.cucumber.java.PendingException();
        }    
    }