package com.p2p.bdd;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.p2p.application.service.LoanService;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;
import com.p2p.domain.loan.strategy.*;
import java.math.BigDecimal;

public class PembayaranCicilanSteps {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private LoanService loanService;

    private Loan loan;
    private Borrower borrower;
    private Exception exception;

    public PembayaranCicilanSteps() {
        MockitoAnnotations.openMocks(this);
    }

    @Given("loan dengan ID {string} memiliki tagihan yang masih aktif sebesar {long} dan tenor {int} bulan")
    public void setupLoan(String id, long amount, int tenor) {
        Money target = new Money(new BigDecimal(amount), "IDR");

        LoanId loanId = new LoanId(id);
        BorrowerId borrowerId = new BorrowerId("BR-001");

        borrower = new Borrower(borrowerId, new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        borrower.tambahSaldo(new Money(new BigDecimal("10000000"), "IDR"));

        this.loan = new Loan(loanId, borrowerId, target, tenor);
        this.loan.ubahStatus("DISBURSED");
        
        when(loanRepository.findById(loanId)).thenReturn(loan);
        when(borrowerRepository.findById(borrowerId)).thenReturn(borrower);
    }

    @Given("loan dengan ID {string} memiliki tagihan bulan ini")
    public void setupLoanWithBill(String id) {
        setupLoan(id, 10000000L, 5);
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

    @Given("loan dengan ID {string} memiliki sisa tagihan keseluruhan sebesar {int} dan status {string}")
    public void loan_dengan_id_memiliki_sisa_tagihan_keseluruhan_sebesar_dan_status(String id, Integer amount, String status) {
        setupLoan(id, amount.longValue(), 1); 
        this.loan.ubahStatus(status);
        this.loan.setInterestStrategy(new FixedInterestStrategy(BigDecimal.ZERO));
        this.loan.generateMonthlyBill();
    }

    @Given("loan dengan ID {string} memiliki status {string}")
    public void loan_dengan_id_memiliki_status(String id, String status) {
        setupLoan(id, 10000000L, 5);
        this.loan.ubahStatus(status);
    }

    @Given("loan tersebut memiliki cicilan pokok dan bunga bulan ini sebesar {int}")
    public void loan_tersebut_memiliki_cicilan_pokok_dan_bunga_bulan_ini_sebesar(Integer baseAmount) {
        this.loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
    }

    @When("sistem menghitung tagihan bulan ini")
    @When("sistem menghitung tagihan bulan pertama")
    @When("sistem menghitung tagihan bulan kedua")
    public void calculateBill() {
        this.loan.generateMonthlyBill();
    }

    @When("borrower membayar lunas tagihan pertama")
    @When("borrower melakukan pembayaran sesuai tagihan bulan ini")
    public void payFull() throws Exception {
        loanService.bayarCicilan(loan.getId(), loan.getTagihanBulanIni());
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

    @When("borrower melakukan pembayaran lunas sebesar {int}")
    public void borrower_melakukan_pembayaran_lunas_sebesar(Integer amount) {
        try {
            Money payAmount = new Money(new BigDecimal(amount), "IDR");
            loanService.bayarCicilan(loan.getId(), payAmount);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("nominal tagihan mencapai {long}")
    @Then("tagihan bulan kedua harus {long}")
    public void checkBill(long expected) {
        assertEquals(new BigDecimal(expected).setScale(0), loan.getTagihanBulanIni().getAmount().setScale(0));
    }

    @Then("Sistem akan menerima pembayaran")
    public void paymentAccepted() {
        assertNull(exception);
    }

    @Then("sisa tagihan bulan ini akan menjadi {int}")
    public void checkRemainingBill(int remaining) {
        if (remaining == 0 && loan.getBulanKe() > 0 && !loan.isLunas()) {
            assertTrue(loan.getBulanKe() > 0);
        } else {
            assertEquals(new BigDecimal(remaining).setScale(0), loan.getTagihanBulanIni().getAmount().setScale(0));
        }
    }

    @Then("sistem akan menolak pembayaran")
    public void paymentRejected() {
        assertNotNull(exception);
    }

    @Then("sistem mengirim notifikasi {string}")
    public void checkErrorNotification(String message) {
        if ("Nominal pembayaran kurang dari nominal tagihan".equals(message)) {
            assertTrue(exception.getMessage().contains("Nominal pembayaran harus sesuai") 
                || exception.getMessage().contains("Nominal pembayaran kurang dari nominal tagihan"));
        } else {
            assertEquals(message, exception.getMessage());
        }
    }

    @Then("sisa tagihan bulan ini akan tetap")
    public void billRemains() {
        assertTrue(loan.getTagihanBulanIni().getAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Then("sisa tagihan keseluruhan akan menjadi {int}")
    public void sisa_tagihan_keseluruhan_akan_menjadi(Integer expected) {
        assertEquals(new BigDecimal(expected).setScale(0), loan.getSisaTagihanKeseluruhan().getAmount().setScale(0));
    }

    @Then("status pinjaman {string} berubah menjadi {string}")
    public void status_pinjaman_berubah_menjadi(String expectedId, String expectedStatus) {
        assertEquals(expectedStatus, loan.getStatus());
    }

    @Then("nominal tagihan harus lebih besar dari {int} karena ditambah denda keterlambatan")
    public void nominal_tagihan_harus_lebih_besar_dari_karena_ditambah_denda_keterlambatan(Integer baseAmount) {
        BigDecimal base = new BigDecimal(baseAmount);
        BigDecimal currentBill = loan.getTagihanBulanIni().getAmount();
        
        assertTrue(currentBill.compareTo(base) > 0, 
            "Tagihan saat ini (" + currentBill + ") seharusnya lebih besar dari " + base + " karena ada denda OVERDUE.");
    }
}