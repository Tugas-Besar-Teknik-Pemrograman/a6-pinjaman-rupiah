package com.p2p.bdd;

import com.p2p.application.service.LoanService;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PengajuanPeminjamanSteps {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private LoanService loanService;

    private Borrower borrower;
    private Loan hasilLoan;
    private Exception exceptionDitolak;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        borrower = new Borrower("01", new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setCreditScore(700);
        borrower.setKycStatus(false);
    }

    @Given("Borrower dengan ID {string} terverifikasi \\(KYC = true)")
    public void borrower_terverifikasi_kyc_true(String id) {
        borrower.setKycStatus(true);
        when(borrowerRepository.findById(id))
                .thenReturn(Optional.of(borrower));
    }

    @Given("Borrower dengan ID {string} terverifikasi \\(KYC = false)")
    public void borrower_tidak_terverifikasi_kyc_false(String id) {
        borrower.setKycStatus(false);
        when(borrowerRepository.findById(id))
                .thenReturn(Optional.of(borrower));
    }

    @Given("Borrower dengan ID {string} terverifikasi")
    public void borrower_terverifikasi(String id) {
        borrower.setKycStatus(true);
        when(borrowerRepository.findById(id))
                .thenReturn(Optional.of(borrower));
    }

    @Given("limit peminjaman {double}")
    public void limit_peminjaman(Double limit) {
        borrower = new Borrower(borrower.getId(), new Money(BigDecimal.valueOf(limit), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        when(borrowerRepository.findById(borrower.getId()))
                .thenReturn(Optional.of(borrower));
    }

    @Given("Credit score Borrower {int}")
    public void credit_score_borrower(Integer score) {
        borrower.setCreditScore(score);
    }

    @When("Borrower mengajukan pinjaman sebesar {double}")
    public void borrower_mengajukan_pinjaman(Double nominal) {
        try {
            hasilLoan = loanService.ajukanPinjaman(borrower.getId(), new Money(BigDecimal.valueOf(nominal), "IDR"));
        } catch (Exception e) {
            exceptionDitolak = e;
        }
    }

    @When("Borrower mengajukan peminjaman sebesar {double}")
    public void borrower_mengajukan_peminjaman_sebesar(Double nominal) {
        borrower_mengajukan_pinjaman(nominal);
    }

    @Then("Sistem membuat Loan dengan status FUNDING")
    public void sistem_membuat_loan_status_funding() {
        assertNull(exceptionDitolak, "Peminjaman seharusnya tidak ditolak, error: " + (exceptionDitolak != null ? exceptionDitolak.getMessage() : ""));
        assertNotNull(hasilLoan);
        assertEquals("FUNDING", hasilLoan.getStatus());
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Then("Sistem akan menolak peminjaman")
    public void sistem_akan_menolak_peminjaman() {
        assertNotNull(exceptionDitolak, "Seharusnya peminjaman ditolak dan melempar Exception");
    }
}