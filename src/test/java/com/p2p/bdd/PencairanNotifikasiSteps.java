package com.p2p.bdd;

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

public class PencairanNotifikasiSteps {

    @Mock private LoanRepository loanRepository;
    @Mock private BorrowerRepository borrowerRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private LoanService loanService;

    private Loan currentLoan;
    private Borrower currentBorrower;
    private Exception thrownException;
    private String notificationResult;

    public PencairanNotifikasiSteps() {
        MockitoAnnotations.openMocks(this);
    }
}