package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.loan.strategy.FixedInterestStrategy;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoanServiceSaldoTest {

    @Test
    void prosesPencairan_menambah_saldo_borrower() {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LenderRepository lenderRepository = mock(LenderRepository.class);

        LoanService loanService = new LoanService(loanRepository, borrowerRepository, lenderRepository, null, null);

        Borrower borrower = new Borrower(new BorrowerId("BR-1"), new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);

        Loan loan = new Loan(new LoanId("LN-1"), borrower.getId(), new Money(new BigDecimal("1000000"), "IDR"), 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.ubahStatus("FUNDING_READY");

        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(borrowerRepository.findById(borrower.getId())).thenReturn(borrower);

        loanService.prosesPencairan(loan.getId());

        assertEquals(0, new BigDecimal("1000000").compareTo(borrower.getSaldoBalance().getAmount()));
        assertEquals("DISBURSED", loan.getStatus());
    }

    @Test
    void bayarCicilan_mengurangi_saldo_borrower_sesuai_tagihan_dan_menyisakan_saldo() throws Exception {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LenderRepository lenderRepository = mock(LenderRepository.class);

        LoanService loanService = new LoanService(loanRepository, borrowerRepository, lenderRepository, null, null);

        Borrower borrower = new Borrower(new BorrowerId("BR-2"), new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        borrower.tambahSaldo(new Money(new BigDecimal("503333"), "IDR"));

        Loan loan = new Loan(new LoanId("LN-2"), borrower.getId(), new Money(new BigDecimal("1000000"), "IDR"), 3);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.ubahStatus("DISBURSED");
        loan.generateMonthlyBill();

        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(borrowerRepository.findById(borrower.getId())).thenReturn(borrower);

        loanService.bayarCicilan(loan.getId(), new Money(new BigDecimal("400000"), "IDR"));

        assertEquals(0, new BigDecimal("120000").compareTo(borrower.getSaldoBalance().getAmount().setScale(0, RoundingMode.HALF_UP)));
        assertEquals("REPAYMENT", loan.getStatus());
    }
}