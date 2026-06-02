package com.p2p.bdd;
import io.cucumber.java.Before;
import com.p2p.application.service.LoanService;
import com.p2p.application.service.NotificationService;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.p2p.application.observer.LoanEventPublisher;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.borrower.BorrowerId;

public class PencairanNotifikasiSteps {

    @Mock private LoanRepository loanRepository;
    @Mock private BorrowerRepository borrowerRepository;
    @Mock private NotificationService notificationService;
    @Mock private LoanEventPublisher loanEventPublisher;
    @InjectMocks private LoanService loanService;
    

    private Loan currentLoan;
    private Borrower currentBorrower;
    private Exception thrownException;
    private String notificationResult;
    private String loanStatusBeforeAction;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        thrownException = null;
        notificationResult = null;
        currentLoan = null;
        currentBorrower = null;
        loanStatusBeforeAction = null;
    }
    
    //Given
    @Given("Loan dengan ID {string} memiliki status {string}")
public void loan_dengan_id_memiliki_status(String loanId, String status) {
    currentLoan = new Loan(new LoanId(loanId), new BorrowerId("BR-001"), new Money(new BigDecimal("10000000"), "IDR"));
    currentLoan.ubahStatus(status);
    when(loanRepository.findById(new LoanId(loanId))).thenReturn(currentLoan);
}

@Given("total dana terkumpul sudah mencapai target {int}")
public void total_dana_terkumpul_sudah_mencapai_target(Integer target) {
    currentLoan.setTotalTerkumpul(new Money(new BigDecimal(target), "IDR"));
    when(loanRepository.findById(currentLoan.getId())).thenReturn(currentLoan);
}

@Given("total dana terkumpul baru mencapai {int} dari target {int}")
public void total_dana_terkumpul_baru_mencapai_dari_target(Integer terkumpul, Integer target) {
    currentLoan.setTotalTerkumpul(new Money(new BigDecimal(terkumpul), "IDR"));
    when(loanRepository.findById(currentLoan.getId())).thenReturn(currentLoan);
}

@Given("Borrower dengan ID {string} terdaftar di sistem")
public void borrower_dengan_id_terdaftar_di_sistem(String borrowerId) {
    currentBorrower = new Borrower(new BorrowerId(borrowerId), new Money(new BigDecimal("50000000"), "IDR"));
    when(borrowerRepository.findById(new BorrowerId(borrowerId))).thenReturn(currentBorrower);

    if (currentLoan != null) {
        String statusLama = currentLoan.getStatus();
        Money targetLama = currentLoan.getTargetNominal();
        LoanId loanId = currentLoan.getId();

        currentLoan = new Loan(loanId, new BorrowerId(borrowerId), targetLama);
        currentLoan.ubahStatus(statusLama);
        when(loanRepository.findById(loanId)).thenReturn(currentLoan);
    }
}

@Given("pencairan untuk Loan {string} ditolak karena dana belum terpenuhi")
public void pencairan_untuk_loan_ditolak_karena_dana_belum_terpenuhi(String loanId) {
    when(loanRepository.findById(new LoanId(loanId))).thenReturn(currentLoan);
}
    
    //When
    @When("sistem memproses pencairan untuk Loan {string}")
public void sistem_memproses_pencairan_untuk_loan(String loanId) {
    try {
        Loan loan = loanRepository.findById(new LoanId(loanId));
        loanStatusBeforeAction = loan.getStatus();
        loanService.prosesPencairan(new LoanId(loanId));
    } catch (Exception e) {
        thrownException = e;
    }
}

@When("sistem mengirimkan notifikasi pencairan untuk Loan {string}")
public void sistem_mengirimkan_notifikasi_pencairan_untuk_loan(String loanId) {
    try {
        Loan loan = loanRepository.findById(new LoanId(loanId));
        loanStatusBeforeAction = loan.getStatus();
        notificationResult = loanService.kirimNotifikasiPencairan(new LoanId(loanId));
    } catch (Exception e) {
        thrownException = e;
    }
}


    //Then
   @Then("status Loan {string} harus berubah menjadi {string}")
public void status_loan_harus_berubah_menjadi(String loanId, String expectedStatus) {
    Loan loan = verifyLoanAndGet(loanId, expectedStatus);
    assertNotEquals(loanStatusBeforeAction, loan.getStatus(), "Status should have changed");
}

@Then("status Loan {string} tetap {string}")
public void status_loan_tetap(String loanId, String expectedStatus) {
    Loan loan = verifyLoanAndGet(loanId, expectedStatus);
    assertEquals(loanStatusBeforeAction, loan.getStatus(), "Status should not have changed");
}

private Loan verifyLoanAndGet(String loanId, String expectedStatus) {
    Loan loan = loanRepository.findById(new LoanId(loanId));
    assertEquals(expectedStatus, loan.getStatus());
    return loan;
}

@Then("Borrower dengan ID {string} harus menerima notifikasi berhasil")
public void borrower_harus_menerima_notifikasi_berhasil(String borrowerId) {
    verify(notificationService, times(1))
        .kirimNotifikasi(eq(new BorrowerId(borrowerId)), contains("berhasil"));
}

@Then("Borrower dengan ID {string} harus menerima notifikasi gagal dengan alasan {string}")
public void borrower_harus_menerima_notifikasi_gagal(String borrowerId, String alasan) {
    verify(notificationService, times(1))
        .kirimNotifikasi(eq(new BorrowerId(borrowerId)), contains(alasan));
}
@Then("sistem harus menolak pencairan dengan pesan error")
public void sistem_harus_menolak_pencairan_dengan_pesan_error() {
    assertNotNull(thrownException);
    assertInstanceOf(IllegalStateException.class, thrownException);
}
}