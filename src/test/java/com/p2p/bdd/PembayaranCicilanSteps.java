package com.p2p.bdd;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.p2p.application.service.LoanService;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;
import com.p2p.domain.loan.strategy.*;
import java.math.BigDecimal;

public class PembayaranCicilanSteps {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    private Loan loan;
    private Exception exception;

    public PembayaranCicilanSteps() {
        MockitoAnnotations.openMocks(this);
    }

    // --- GIVEN ---
    @Given("loan dengan ID {string} memiliki tagihan yang masih aktif sebesar {long} dan tenor {int} bulan")
    public void setupLoan(String id, long amount, int tenor) {
        Money target = new Money(new BigDecimal(amount), "IDR");
        this.loan = new Loan(id, "BR-001", target, tenor);
        this.loan.ubahStatus("DISBURSED");
        
        when(loanRepository.findById(id)).thenReturn(loan);
    }

    @Given("loan dengan ID {string} memiliki tagihan bulan ini")
    public void setupLoanWithBill(String id) {
        setupLoan(id, 10000000, 5);
        this.loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        this.loan.generateMonthlyBill();
    }

    @Given("loan tersebut menggunakan bunga fixed sebesar {int}%")
    public void setFixedInterest(int rate) {
        BigDecimal decimalRate = new BigDecimal(rate).divide(new BigDecimal("100"));
        this.loan.setInterestStrategy(new FixedInterestStrategy(decimalRate));
    }

    @Given("loan tersebut menggunakan bunga syariah dengan margin flat sebesar {int}")
    public void setSyariahMargin(int margin) {
        this.loan.setInterestStrategy(new SyariahInterestStrategy(new BigDecimal(margin)));
    }

    @Given("loan tersebut menggunakan bunga float sebesar {int}%")
    public void setFloatingInterest(int rate) {
        BigDecimal decimalRate = new BigDecimal(rate).divide(new BigDecimal("100"));
        this.loan.setInterestStrategy(new FloatingInterestStrategy(decimalRate));
    }

    // --- WHEN ---
    @When("sistem menghitung tagihan bulan ini")
    @When("sistem menghitung tagihan bulan pertama")
    @When("sistem menghitung tagihan bulan kedua")
    public void calculateBill() {
        this.loan.generateMonthlyBill();
    }

    @When("borrower membayar lunas tagihan pertama")
    @When("borrower melakukan pembayaran sesuai tagihan bulan ini")
    public void payFull() throws Exception {
        loanService.bayarCicilan(loan.getId(), loan.getCurrentMonthBill());
    }

    @When("borrower melakukan pembayaran kurang dari tagihan bulan ini")
    public void payLess() {
        try {
            Money tooSmall = new Money(new BigDecimal("1000"), "IDR");
            loanService.bayarCicilan(loan.getId(), tooSmall);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    // --- THEN ---
    @Then("nominal tagihan mencapai {long}")
    @Then("tagihan bulan kedua harus {long}")
    public void checkBill(long expected) {
        assertEquals(new BigDecimal(expected).setScale(0), loan.getCurrentMonthBill().getAmount().setScale(0));
    }

    @Then("Sistem akan menerima pembayaran")
    public void paymentAccepted() {
        assertNull(exception);
    }

    @Then("sisa tagihan bulan ini akan menjadi {int}")
    public void checkRemainingBill(int remaining) {
        assertEquals(new BigDecimal(remaining).setScale(0), loan.getCurrentMonthBill().getAmount().setScale(0));
    }

    @Then("sistem akan menolak pembayaran")
    public void paymentRejected() {
        assertNotNull(exception);
    }

    @Then("sistem mengirim notifikasi {string}")
    public void checkErrorNotification(String message) {
        assertEquals(message, exception.getMessage());
    }

    @Then("sisa tagihan bulan ini akan tetap")
    public void billRemains() {
        assertTrue(loan.getCurrentMonthBill().getAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}
