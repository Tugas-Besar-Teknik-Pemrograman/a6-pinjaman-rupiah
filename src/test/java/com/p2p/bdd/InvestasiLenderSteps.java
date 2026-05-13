package com.p2p.bdd;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;

import com.p2p.application.service.FundingService;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;
import com.p2p.infrastructure.memory.RepositoryFactory;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class InvestasiLenderSteps {
    
    LoanRepository loanRepository;
    LenderRepository lenderRepository;
    FundingService fundingService;

    Loan loan;
    Lender lender;
    Money investmentAmount;
    Exception caughtException;
    
    public InvestasiLenderSteps() {
        this.loanRepository = RepositoryFactory.getInstance().getLoanRepository();
        this.lenderRepository = RepositoryFactory.getInstance().getLenderRepository();
        this.fundingService = new FundingService(loanRepository, lenderRepository);
    }

    // Givern
    @Given("Loan dengan status FUNDING")
    public void loan_dengan_status_funding() {
        // 1. buat loan dengan target 10 juta
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        loan = new Loan("LN-001", "BR-001", target, 12);
        loan.ubahStatus("FUNDING");

        // 2. Simpan loan ke repository in-memory
        loanRepository.save(loan);
        
        // 3. Simpan lender ke repository in-memory
        lender = new Lender("LDR-001", new Money(new BigDecimal("10000000"), "IDR"));
        lenderRepository.save(lender);
    }

    @Given("Loan dengan status not FUNDING")
    public void loan_dengan_status_not_funding() {
        // 1. Buat Loan seperti biasa
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        loan = new Loan("LN-001", "BR-001", target, 12); // Kita pakai LN-001 agar matching dengan fungsi @When
        
        // 2. TAPI, statusnya kita set selain FUNDING (misal: PROPOSED)
        loan.ubahStatus("PROPOSED"); 
        
        // 3. Simpan loan ke repository in-memory
        loanRepository.save(loan);
        
        // 4. Simpan lender ke repository in-memory
        lender = new Lender("LDR-001", new Money(new BigDecimal("10000000"), "IDR"));
        lenderRepository.save(lender);
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
        
        // Tambah saldo lender agar error yang dilempar bukan saldo tidak cukup
        lender.tambahSaldo(new Money(new BigDecimal("10000000"), "IDR"));
        lenderRepository.save(lender);

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
        
        Loan updatedLoan = loanRepository.findById("LN-001");
        Lender updatedLender = lenderRepository.findById("LDR-001");

        Assertions.assertEquals(new BigDecimal("5000000"), updatedLoan.getTotalTerkumpul().getAmount());
        Assertions.assertEquals(new BigDecimal("5000000"), updatedLender.getSaldoBalance().getAmount(), "Saldo lender harus berkurang");
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
