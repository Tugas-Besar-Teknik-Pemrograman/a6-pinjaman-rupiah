package com.p2p.bdd;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.valueobject.Money;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PenarikanSaldoLenderSteps {

    private Lender lender;
    private Money withdrawalAmount;
    private Exception caughtException;

    // Given
    @Given("Loan ID {string} dengan terverifikasi \\(KYC = true)")
    public void loan_id_dengan_terverifikasi_kyc_true(String lenderId) {
        Money initialBalance = new Money(new BigDecimal("5000000"), "IDR");
        lender = new Lender(lenderId, initialBalance);
        lender.setKycStatus(true);
    }

    @Given("Loan ID {string} dengan terverifikasi \\(KYC = false)")
    public void loan_id_dengan_terverifikasi_kyc_false(String lenderId) {
        Money initialBalance = new Money(new BigDecimal("5000000"), "IDR");
        lender = new Lender(lenderId, initialBalance);
        lender.setKycStatus(false);
    }

    // When
    @When("Lender mengajukan penarikan dana < 100k")
    public void lender_mengajukan_penarikan_dana_kurang_dari_100k() {
        withdrawalAmount = new Money(new BigDecimal("50000"), "IDR");

        try {
            lender.tarikSaldo(withdrawalAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("Lender mengajukan penarikan dana > 100k")
    public void lender_mengajukan_penarikan_dana_lebih_dari_100k() {
        withdrawalAmount = new Money(new BigDecimal("500000"), "IDR");

        try {
            lender.tarikSaldo(withdrawalAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    // Then
    @Then("Sistem akan menolak dengan pesan error")
    public void sistem_akan_menolak_dengan_pesan_error() {
        assertNotNull(caughtException, "Seharusnya penarikan ditolak karena nominal < 100k");
        assertEquals("Nominal penarikan minimal harus 100000", caughtException.getMessage());
    }

    @Then("Sistem akan menolak penarikan dana dengan pesan error")
    public void sistem_akan_menolak_penarikan_dana_dengan_pesan_error() {
        assertNotNull(caughtException, "Seharusnya penarikan ditolak karena KYC = false");
        assertEquals("Lender tidak terverifikasi (KYC = false)", caughtException.getMessage());
    }

    @Then("Sistem akan mengurangi saldo tersedia dengan jumlah penarikan yang di input")
    public void sistem_akan_mengurangi_saldo_tersedia_dengan_jumlah_penarikan_yang_di_input() {
        assertNull(caughtException, "Seharusnya penarikan berhasil tanpa error");
        assertEquals(new BigDecimal("4500000"), lender.getSaldoBalance().getAmount());
    }
}
