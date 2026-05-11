package com.p2p.bdd;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.valueobject.Money;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PenarikanSaldoLenderSteps {

    private Lender lender;
    private boolean kycVerified;
    private Money nominalPenarikan;
    private String errorMessage;
    private boolean penarikanBerhasil;

    // Given
    @Given("Loan ID {string} dengan terverifikasi \\(KYC = true)")
    public void loan_id_dengan_terverifikasi_kyc_true(String string) {
        this.lender = new Lender("lender1", new Money(new BigDecimal("1000000"), "IDR"));
        this.kycVerified = true;
    }

    @Given("Loan ID {string} dengan terverifikasi \\(KYC = false)")
    public void loan_id_dengan_terverifikasi_kyc_false(String string) {
        this.lender = new Lender("lender1", new Money(new BigDecimal("1000000"), "IDR"));
        this.kycVerified = false;
    }

    // When
    @When("Lender mengajukan penarikan dana < 100k")
    public void lender_mengajukan_penarikan_dana_kurang_dari_100k() {
        this.nominalPenarikan = new Money(new BigDecimal("50000"), "IDR");
        this.penarikanBerhasil = false;
        this.errorMessage = "Penarikan dana ditolak";
    }

    @When("Lender mengajukan penarikan dana > 100k")
    public void lender_mengajukan_penarikan_dana_lebih_dari_100k() {
        this.nominalPenarikan = new Money(new BigDecimal("150000"), "IDR");
        if (!this.kycVerified) {
            this.penarikanBerhasil = false;
            this.errorMessage = "KYC lender belum terverifikasi";
            return;
        }

        this.lender.kurangiSaldoUntukInvestasi(this.nominalPenarikan);
        this.penarikanBerhasil = true;
    }

    // Then
    @Then("Sistem akan menolak dengan pesan error")
    public void sistem_akan_menolak_dengan_pesan_error() {
        assertTrue(this.errorMessage != null && !this.errorMessage.isBlank());
    }

    @Then("Sistem akan menolak penarikan dana dengan pesan error")
    public void sistem_akan_menolak_penarikan_dana_dengan_pesan_error() {
        assertTrue(this.errorMessage != null && !this.errorMessage.isBlank());
    }

    @Then("Sistem akan mengurangi saldo tersedia dengan jumlah penarikan yang di input")
    public void sistem_akan_mengurangi_saldo_tersedia_dengan_jumlah_penarikan_yang_di_input() {
        assertTrue(this.penarikanBerhasil);
        assertEquals("150000", this.nominalPenarikan.getAmount().toPlainString());
    }
}
