package com.p2p.bdd;

import com.p2p.application.service.LoanService;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
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
    public void initialState() {
        MockitoAnnotations.openMocks(this);

        BorrowerId dummyId = new BorrowerId("01");
        Money limitAwal = new Money(new BigDecimal("10000000"), "IDR");

        borrower = new Borrower(dummyId, limitAwal);
        hasilLoan = null;
        exceptionDitolak = null;
    }

    @Given("Borrower dengan ID {string} terverifikasi \\(KYC = true)")
    public void borrower_dengan_id_terverifikasi_kyc_true(String id) {
        BorrowerId bId = new BorrowerId(id);

        borrower.setId(bId);
        borrower.setKycStatus(true);
        when(borrowerRepository.findById(bId)).thenReturn(borrower);
    }

    @Given("Borrower dengan ID {string} terverifikasi \\(KYC = false)")
    public void borrower_dengan_id_terverifikasi_kyc_false(String id) {
        BorrowerId bId = new BorrowerId(id);

        borrower.setId(bId);
        borrower.setKycStatus(false);
        when(borrowerRepository.findById(bId)).thenReturn(borrower);
    }

    @Given("Borrower dengan ID {string} terverifikasi")
    public void borrower_dengan_id_terverifikasi(String id) {
        BorrowerId bId = new BorrowerId(id);

        borrower.setId(bId);
        borrower.setKycStatus(true);
        when(borrowerRepository.findById(bId)).thenReturn(borrower);
    }
    @Given("limit peminjaman {double}")
    public void limit_peminjaman(Double limit) {
        borrower.setLimitPinjaman(new Money(new BigDecimal(limit), "IDR"));
    }

    @Given("Credit score Borrower {int}")
    public void credit_score_borrower(Integer score) {
        borrower.setCreditScore(score);
    }

    @Given("Borrower {string} memiliki pinjaman aktif dengan status {string}")
    public void borrower_memiliki_pinjaman_aktif_dengan_status(String string, String string2) {
        borrower.setHasActiveLoan(true);
    }

    @When("Borrower mengajukan peminjaman sebesar {double}")
    public void borrower_mengajukan_peminjaman_sebesar(Double nominal) {
        try {
            Money nominalPinjaman = new Money(new BigDecimal(nominal), "IDR");
            int tenor = 12;
            hasilLoan = loanService.ajukanPinjaman(borrower.getId(), nominalPinjaman, tenor);
        } catch (Exception e) {
            exceptionDitolak = e;
        }
    }

    @Then("Sistem membuat Loan dengan status FUNDING")
    public void sistem_membuat_loan_dengan_status_funding() {
        assertNull(exceptionDitolak, "Pengajuan harusnya berhasil, ga error");
        assertNotNull(hasilLoan, "Objek Loan harusnya terbentuk");
        assertEquals("FUNDING", hasilLoan.getStatus().toString());

        verify(borrowerRepository, times(1)).save(any(Borrower.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Then("Sistem akan menolak peminjaman")
    public void sistem_akan_menolak_peminjaman() {
        assertNotNull(exceptionDitolak, "Sistem harusnya menolak dan melempar Exception");
        assertNull(hasilLoan, "Objek Loan tidak boleh terbentuk");
    }

    @Then("Sistem akan menolak peminjaman dengan pesan {string}")
    public void sistem_akan_menolak_peminjaman_dengan_pesan(String pesanErrorExpected) {
        assertNotNull(exceptionDitolak, "Sistem harusnya menolak dan melempar Exception");
        assertEquals(pesanErrorExpected, exceptionDitolak.getMessage());
    }
}