package com.p2p.bdd;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.p2p.application.service.FundingService;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class InvestasiLenderSteps {
    
    @Mock
    LoanRepository loanRepository;

    @Mock
    LenderRepository lenderRepository;

    @InjectMocks
    FundingService fundingService;

    Loan loan;
    Money investmentAmount;
    Exception caughtException;
    
    public InvestasiLenderSteps() {
        MockitoAnnotations.openMocks(this);
    }

    // Givern
    @Given("Loan dengan status FUNDING")
    public void loan_dengan_status_funding() {
        // 1. buat loan dengan target 10 juta
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        loan = new Loan("LN-001", "BR-001", target);
        loan.ubahStatus("FUNDING");

        // 2. Kaish instruksi ke Mockito: "Kalau FundingService mencari data 'LN-001', berikan si loan ini!"
        when(loanRepository.findById("LN-001")).thenReturn(loan);
    }

    @Given("Loan dengan status not FUNDING")
    public void loan_dengan_status_not_funding() {
        // 1. Buat Loan seperti biasa
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        loan = new Loan("LN-001", "BR-001", target); // Kita pakai LN-001 agar matching dengan fungsi @When
        
        // 2. TAPI, statusnya kita set selain FUNDING (misal: PROPOSED)
        loan.ubahStatus("PROPOSED"); 
        
        // 3. Kasih tahu Mockito
        when(loanRepository.findById("LN-001")).thenReturn(loan);
    }
    
    // When
    @When("Lender input dana yang ingin diberikan")
    public void lender_input_dana_yang_ingin_diberikan() {
        // 1. Set uang investasi sebanyak 5 Juta
        investmentAmount = new Money(new BigDecimal("5000000"), "IDR");
        
        try {
            fundingService.invest("LDR-001", "LN-001", investmentAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("Lender input dana investasi <= {int}")
    public void lender_input_dana_investasi(Integer angkaLimit) {
        // angkaLimit akan bernilai 0 (karena di file .feature tertulis <= 0)
        investmentAmount = new Money(new BigDecimal(angkaLimit), "IDR");
        
        try {
            // Coba lakukan investasi dengan uang 0 Rupiah!
            fundingService.invest("LDR-001", "LN-001", investmentAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("Lender input dana yang ingin diberikan > target")
    public void lender_input_dana_yang_ingin_diberikan_target() {
        // Kita paksa masukkan uang 15 Juta (melebihi target 10 Juta)
        investmentAmount = new Money(new BigDecimal("15000000"), "IDR");
        
        try {
            fundingService.invest("LDR-001", "LN-001", investmentAmount);
        } catch (Exception e) {
            caughtException = e;
        }
    }
    
    // Then
    @Then("Loan akan akan terisi sesuai nominal dana yang di input")
    public void loan_akan_akan_terisi_sesuai_nominal_dana_yang_di_input() {
        Assertions.assertNull(caughtException, "Seharusnya investasi berhasil dan tidak ada error");
        
        Assertions.assertEquals(new BigDecimal("5000000"), loan.getTotalTerkumpul().getAmount());
    }
    
    @Then("Sistem akan menolak dengan pesan error karena status not FUNDING")
    public void sistem_akan_menolak_dengan_pesan_error_karena_status_not_funding() {
        Assertions.assertNotNull(caughtException, "Seharusnya investasi ditolak dan melempar error!");

        Assertions.assertEquals("Investasi ditolak, status Loan bukan FUNDING", caughtException.getMessage());
    }
    
    @Then("Sistem harus menolak pengajuan dengan pesan error")
    public void sistem_harus_menolak_pengajuan_dengan_pesan_error() {
        // Kita berharap ada error yang ditangkap karena uangnya 0
        Assertions.assertNotNull(caughtException, "Seharusnya investasi ditolak karena nominal 0 atau negatif!");
        
        // Kita harapkan pesan errornya seperti ini
        Assertions.assertEquals("Nominal investasi harus lebih dari 0", caughtException.getMessage());
    }
    
    @Then("Sistem akan menolak investasi dengan pesan error")
    public void sistem_akan_menolak_investasi_dengan_pesan_error() {
        Assertions.assertNotNull(caughtException, "Seharusnya investasi ditolak karena melebihi target!");
        Assertions.assertEquals("Nominal investasi melebihi target pendanaan", caughtException.getMessage());
    }


}
