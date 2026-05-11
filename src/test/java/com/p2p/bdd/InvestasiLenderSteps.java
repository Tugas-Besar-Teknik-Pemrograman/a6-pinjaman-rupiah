package com.p2p.bdd;

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
        
    }

    @Given("Loan dengan status not FUNDING")
    public void loan_dengan_status_not_funding() {
        
    }
    
    // When
    @When("Lender input dana yang ingin diberikan")
    public void lender_input_dana_yang_ingin_diberikan() {
        
    }

    @When("Lender input dana investasi <= {int}")
    public void lender_input_dana_investasi(Integer int1) {
        
    }

    @When("Lender input dana yang ingin diberikan > target")
    public void lender_input_dana_yang_ingin_diberikan_target() {

    }
    
    // Then
    @Then("Loan akan akan terisi sesuai nominal dana yang di input")
    public void loan_akan_akan_terisi_sesuai_nominal_dana_yang_di_input() {
        
    }
    
    @Then("Sistem akan menolak dengan pesan error karena status not FUNDING")
    public void sistem_akan_menolak_dengan_pesan_error_karena_status_not_funding() {
        
    }
    
    @Then("Sistem harus menolak pengajuan dengan pesan error")
    public void sistem_harus_menolak_pengajuan_dengan_pesan_error() {
        
    }
    
    @Then("Sistem akan menolak dengan pesan error")
    public void sistem_akan_menolak_dengan_pesan_error() {

    }
}
