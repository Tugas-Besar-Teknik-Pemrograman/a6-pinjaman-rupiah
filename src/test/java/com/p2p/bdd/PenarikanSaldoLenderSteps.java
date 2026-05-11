package com.p2p.bdd;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.valueobject.Money;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class PenarikanSaldoLenderSteps {

    @Mock
    LenderRepository lenderRepository;

    Lender lender;
    Money withdrawalAmount;
    Exception caughtException;

    public PenarikanSaldoLenderSteps() {
        MockitoAnnotations.openMocks(this);
    }

    // Given
    @Given("Loan ID {string} dengan terverifikasi \\(KYC = true)")
    public void loan_id_dengan_terverifikasi_kyc_true(String lenderId) {
        // 1. Buat Lender dengan saldo awal 5 juta dan KYC = true
        Money initialBalance = new Money(new BigDecimal("5000000"), "IDR");
        lender = new Lender(lenderId, initialBalance);
        lender.setKycStatus(true);
        
        // 2. Mock: kalau cari Lender dengan ID ini, berikan si lender
        when(lenderRepository.findById(lenderId)).thenReturn(lender);
    }

    @Given("Loan ID {string} dengan terverifikasi \\(KYC = false)")
    public void loan_id_dengan_terverifikasi_kyc_false(String lenderId) {
        // 1. Buat Lender dengan saldo awal 5 juta tapi KYC = false
        Money initialBalance = new Money(new BigDecimal("5000000"), "IDR");
        lender = new Lender(lenderId, initialBalance);
        lender.setKycStatus(false); // Eksplisit set false
        
        // 2. Mock setup
        when(lenderRepository.findById(lenderId)).thenReturn(lender);
    }

    // When
    @When("Lender mengajukan penarikan dana < 100k")
    public void lender_mengajukan_penarikan_dana_kurang_dari_100k() {
        // Coba tarik 50k (kurang dari minimum 100k)
        withdrawalAmount = new Money(new BigDecimal("50000"), "IDR");
        
        try {
            lender.tarikSaldo(withdrawalAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("Lender mengajukan penarikan dana > 100k")
    public void lender_mengajukan_penarikan_dana_lebih_dari_100k() {
        // Tarik 500k (lebih dari minimum 100k)
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
        Assertions.assertNotNull(caughtException, "Seharusnya penarikan ditolak karena nominal < 100k");
        Assertions.assertEquals("Nominal penarikan minimal harus 100000", caughtException.getMessage());
    }

    @Then("Sistem akan menolak penarikan dana dengan pesan error")
    public void sistem_akan_menolak_penarikan_dana_dengan_pesan_error() {
        Assertions.assertNotNull(caughtException, "Seharusnya penarikan ditolak karena KYC = false");
        Assertions.assertEquals("Lender tidak terverifikasi (KYC = false)", caughtException.getMessage());
    }

    @Then("Sistem akan mengurangi saldo tersedia dengan jumlah penarikan yang di input")
    public void sistem_akan_mengurangi_saldo_tersedia_dengan_jumlah_penarikan_yang_di_input() {
        Assertions.assertNull(caughtException, "Seharusnya penarikan berhasil tanpa error");
        
        // Saldo awal 5 juta, dikurang 500k = 4.5 juta
        BigDecimal expectedBalance = new BigDecimal("4500000");
        Assertions.assertEquals(expectedBalance, lender.getSaldoBalance().getAmount());
    }
}

