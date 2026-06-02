package com.p2p.bdd;

import com.p2p.application.service.WithdrawalService;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
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

public class PenarikanSaldoBorrowerSteps {

    private final BorrowerRepository borrowerRepository;
    private final WithdrawalService withdrawalService;

    private BorrowerId currentBorrowerId;
    private Exception caughtException;

    public PenarikanSaldoBorrowerSteps() {
        this.borrowerRepository = RepositoryFactory.getInstance().getBorrowerRepository();
        this.withdrawalService = new WithdrawalService(
                RepositoryFactory.getInstance().getLenderRepository(),
                borrowerRepository
        );
    }

    @After
    public void tearDown() {
        RepositoryFactory.getInstance().clearData();
    }

    @Given("Borrower ID {string} dengan terverifikasi \\(KYC = true)")
    public void borrower_id_dengan_terverifikasi_kyc_true(String borrowerId) {
        this.currentBorrowerId = new BorrowerId(borrowerId);
        Money initialPenghasilan = new Money(new BigDecimal("10000000"), "IDR");
        Borrower borrower = new Borrower(currentBorrowerId, initialPenghasilan);
        borrower.setKycStatus(true);
        borrower.tambahSaldo(new Money(new BigDecimal("5000000"), "IDR"));
        borrowerRepository.save(borrower);
    }

    @Given("Borrower ID {string} dengan terverifikasi \\(KYC = false)")
    public void borrower_id_dengan_terverifikasi_kyc_false(String borrowerId) {
        this.currentBorrowerId = new BorrowerId(borrowerId);
        Money initialPenghasilan = new Money(new BigDecimal("10000000"), "IDR");
        Borrower borrower = new Borrower(currentBorrowerId, initialPenghasilan);
        borrower.setKycStatus(false);
        borrower.tambahSaldo(new Money(new BigDecimal("5000000"), "IDR"));
        borrowerRepository.save(borrower);
    }

    @Given("Saldo borrower tersedia hanya {int}k")
    public void saldo_borrower_tersedia_hanya(Integer amount) {
        Borrower borrower = borrowerRepository.findById(currentBorrowerId);
        Borrower updatedBorrower = new Borrower(currentBorrowerId, borrower.getPenghasilan());
        updatedBorrower.setKycStatus(borrower.isKycStatus());
        if (amount > 0) {
            updatedBorrower.tambahSaldo(new Money(new BigDecimal(amount * 1000), "IDR"));
        }
        borrowerRepository.save(updatedBorrower);
    }

    @When("Borrower mengajukan penarikan dana < 100k")
    public void borrower_mengajukan_penarikan_dana_kurang_dari_100k() {
        Money withdrawalAmount = new Money(new BigDecimal("50000"), "IDR");

        try {
            withdrawalService.withdraw(currentBorrowerId, withdrawalAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("Borrower mengajukan penarikan dana > 100k")
    public void borrower_mengajukan_penarikan_dana_lebih_dari_100k() {
        Money withdrawalAmount = new Money(new BigDecimal("500000"), "IDR");

        try {
            withdrawalService.withdraw(currentBorrowerId, withdrawalAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("Borrower mengajukan penarikan dana {int} juta")
    public void borrower_mengajukan_penarikan_dana_juta(Integer amount) {
        Money withdrawalAmount = new Money(new BigDecimal(amount * 1000000), "IDR");

        try {
            withdrawalService.withdraw(currentBorrowerId, withdrawalAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("Sistem akan menolak penarikan borrower dengan pesan error {string}")
    public void sistem_akan_menolak_penarikan_borrower_dengan_pesan_error(String expectedMessage) {
        assertNotNull(caughtException, "Seharusnya penarikan ditolak");
        assertEquals(expectedMessage, caughtException.getMessage());
    }

    @Then("Sistem akan mengurangi saldo borrower dengan jumlah penarikan yang di input")
    public void sistem_akan_mengurangi_saldo_borrower_dengan_jumlah_penarikan_yang_di_input() {
        assertNull(caughtException, "Seharusnya penarikan berhasil tanpa error");
        Borrower borrower = borrowerRepository.findById(currentBorrowerId);
        assertEquals(0, new BigDecimal("4500000").compareTo(borrower.getSaldoBalance().getAmount()));
    }

    @Then("Sistem akan menolak penarikan borrower karena saldo tidak cukup")
    public void sistem_akan_menolak_penarikan_borrower_karena_saldo_tidak_cukup() {
        assertNotNull(caughtException, "Seharusnya ditolak karena saldo tidak cukup");
        assertEquals("Saldo tidak mencukupi untuk melakukan penarikan", caughtException.getMessage());
    }
}
