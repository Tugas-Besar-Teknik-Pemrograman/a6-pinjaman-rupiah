package com.p2p.domain;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SiklusStatusTest {

        private Borrower borrower;
        private Loan loan;

        @BeforeEach
        public void setUp() {
            borrower = new Borrower("borrower1", new Money(new BigDecimal("50000000"), "IDR"));
            loan = new Loan("loan1", borrower.getId(), new Money(new BigDecimal("10000000"), "IDR"));
            borrower.setKycStatus(true);
        }

        // 1. Borrower mengajukan pinjaman
        @Test
        public void TestBorrowerMengajukanPinjaman() {
            loan.ubahStatus("FUNDING");
            assertEquals("FUNDING", loan.getStatus());
        }
}
