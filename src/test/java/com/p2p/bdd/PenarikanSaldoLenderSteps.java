package com.p2p.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class PenarikanSaldoLenderSteps {

    // Given
    @Given("Loan ID {string} dengan terverifikasi \\(KYC = true)")
    public void loan_id_dengan_terverifikasi_kyc_true(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("Loan ID {string} dengan terverifikasi \\(KYC = false)")
    public void loan_id_dengan_terverifikasi_kyc_false(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    // When
    @When("Lender mengajukan penarikan dana < 100k")
    public void lender_mengajukan_penarikan_dana_100k() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    // Then
    @Then("Sistem akan menolak dengan pesan error")
    public void sistem_akan_menolak_dengan_pesan_error() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("Sistem akan menolak penarikan dana dengan pesan error")
    public void sistem_akan_menolak_penarikan_dana_dengan_pesan_error() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("Sistem akan mengurangi saldo tersedia dengan jumlah penarikan yang di input")
    public void sistem_akan_mengurangi_saldo_tersedia_dengan_jumlah_penarikan_yang_di_input() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
}
