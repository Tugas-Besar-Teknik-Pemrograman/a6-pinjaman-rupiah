package com.p2p.domain;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BorrowerTest {

    private Borrower borrower;

    @BeforeEach
    void inisialisasi() {
        borrower = new Borrower("B-001", new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        borrower.setHasActiveLoan(false);
    }

    @Test
    void ajukanPinjaman_Sukses_LimitBerkurangDanLoanTerbentuk() {
        // Arrange
        Money nominal = new Money(new BigDecimal("2000000"), "IDR");

        // Act
        Loan loanBaru = borrower.ajukanPinjaman("001",nominal);

        // Assert
        assertNotNull(loanBaru, "Loan harus berhasil dibuat");
        assertEquals("001", loanBaru.getId());

        //
        BigDecimal sisaLimitExpected = new BigDecimal("8000000");
        assertEquals(sisaLimitExpected, borrower.getLimitPinjaman().getAmount());
    }

}