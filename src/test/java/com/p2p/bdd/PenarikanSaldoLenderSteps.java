package com.p2p.bdd;

import com.p2p.application.service.WithdrawalService;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.valueobject.Money;
import com.p2p.infrastructure.memory.RepositoryFactory;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PenarikanSaldoLenderSteps {

    private final LenderRepository lenderRepository;
    private final WithdrawalService withdrawalService;

    private LenderId currentLenderId;
    private Exception caughtException;

    public PenarikanSaldoLenderSteps() {
        this.lenderRepository = RepositoryFactory.getInstance().getLenderRepository();
        this.withdrawalService = new WithdrawalService(lenderRepository);
    }

    @After
    public void tearDown() {
        RepositoryFactory.getInstance().clearData();
    }

    // Given
    @Given("Lender ID {string} dengan terverifikasi \\(KYC = true)")
    public void lender_id_dengan_terverifikasi_kyc_true(String lenderId) {
        this.currentLenderId = new LenderId(lenderId);
        Money initialBalance = new Money(new BigDecimal("5000000"), "IDR");
        Lender lender = new Lender(currentLenderId, initialBalance);
        lender.setKycStatus(true);
        lenderRepository.save(lender);
    }

    @Given("Lender ID {string} dengan terverifikasi \\(KYC = false)")
    public void lender_id_dengan_terverifikasi_kyc_false(String lenderId) {
        this.currentLenderId = new LenderId(lenderId);
        Money initialBalance = new Money(new BigDecimal("5000000"), "IDR");
        Lender lender = new Lender(currentLenderId, initialBalance);
        lender.setKycStatus(false);
        lenderRepository.save(lender);
    }

    @Given("Saldo tersedia hanya {int}k")
    public void saldo_tersedia_hanya(Integer amount) {
        Lender lender = lenderRepository.findById(currentLenderId);
        Money newBalance = new Money(new BigDecimal(amount * 1000), "IDR");
        Lender updatedLender = new Lender(currentLenderId, newBalance);
        updatedLender.setKycStatus(lender.isKycVerified());
        lenderRepository.save(updatedLender);
    }

    // When
    @When("Lender mengajukan penarikan dana < 100k")
    public void lender_mengajukan_penarikan_dana_kurang_dari_100k() {
        Money withdrawalAmount = new Money(new BigDecimal("50000"), "IDR");

        try {
            withdrawalService.withdraw(currentLenderId, withdrawalAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("Lender mengajukan penarikan dana > 100k")
    public void lender_mengajukan_penarikan_dana_lebih_dari_100k() {
        Money withdrawalAmount = new Money(new BigDecimal("500000"), "IDR");

        try {
            withdrawalService.withdraw(currentLenderId, withdrawalAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("Lender mengajukan penarikan dana {int} juta")
    public void lender_mengajukan_penarikan_dana_juta(Integer amount) {
        Money withdrawalAmount = new Money(new BigDecimal(amount * 1000000), "IDR");

        try {
            withdrawalService.withdraw(currentLenderId, withdrawalAmount);
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
        Lender lender = lenderRepository.findById(currentLenderId);
        assertEquals(0, new BigDecimal("4500000").compareTo(lender.getSaldoBalance().getAmount()));
    }

    @Then("Sistem akan menolak karena saldo tidak cukup")
    public void sistem_akan_menolak_saldo_tidak_cukup() {
        assertNotNull(caughtException, "Seharusnya ditolak karena saldo tidak cukup");
        assertEquals("Saldo tidak mencukupi untuk melakukan penarikan", caughtException.getMessage());
    }
}
