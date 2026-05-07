package com.p2p;

import com.p2p.domain.Borrower;
import com.p2p.domain.Loan;
import com.p2p.service.LoanService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
public class LoanServiceTest {
    @Test
    void shouldRejectLoanWhenBorrowerNotVerified() {

        // SCENARIO:
        // Borrower tidak terverifikasi (KYC = false)
        // Ketika borrower mengajukan pinjaman
        // Maka sistem harus menolak dengan melempar exception

        // Arrange (Initial Condition)
        // Borrower belum lolos proses KYC
        Borrower borrower = new Borrower(false, 700);

        // Service untuk pengajuan loan
        LoanService loanService = new LoanService();

        // Jumlah pinjaman valid
        BigDecimal amount = BigDecimal.valueOf(1000);

        // Act (Action)
        // Borrower mencoba mengajukan loan
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.createLoan(borrower, amount);
        });
    }

    @Test
    void shouldRejectLoanWhenAmountIsZeroOrNegative() {
        // Arrange
        Borrower borrower = new Borrower(true, 700); // borrower valid
        LoanService loanService = new LoanService();
        BigDecimal amount = BigDecimal.ZERO; // amount = 0

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> {
            loanService.createLoan(borrower, amount);
        });
    }

    @Test
    void shouldApproveLoanWhenCreditScoreHigh() {
        // Arrange
        Borrower borrower = new Borrower(true, 700); // verified, credit score tinggi
        LoanService loanService = new LoanService();
        BigDecimal amount = BigDecimal.valueOf(1000);

        // Act
        Loan loan = loanService.createLoan(borrower, amount);

        // Assert
        assertEquals(Loan.Status.APPROVED, loan.getStatus());
    }

    @Test
    void shouldRejectLoanWhenCreditScoreLow() {
        // Arrange
        Borrower borrower = new Borrower(true, 500); // verified, credit score rendah
        LoanService loanService = new LoanService();
        BigDecimal amount = BigDecimal.valueOf(1000);

        // Act
        Loan loan = loanService.createLoan(borrower, amount);

        // Assert
        assertEquals(Loan.Status.REJECTED, loan.getStatus());
    }
}
