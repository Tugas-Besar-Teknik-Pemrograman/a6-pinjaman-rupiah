package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
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

    @Test
    void simulasiTenorBerikutnya_menghasilkan_tagihan_baru() throws Exception {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        
        LoanService loanService = new LoanService(loanRepository, borrowerRepository, null, null);
        
        Borrower borrower = new Borrower(new BorrowerId("BR-3"), new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        borrower.tambahSaldo(new Money(new BigDecimal("5000000"), "IDR"));
        
        Loan loan = new Loan(new LoanId("LN-3"), borrower.getId(), new Money(new BigDecimal("1000000"), "IDR"), 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.ubahStatus("DISBURSED");
        loan.generateMonthlyBill(); // tagihan awal terbentuk
        
        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(borrowerRepository.findById(borrower.getId())).thenReturn(borrower);
        
        // Bayar cicilan pertama
        loanService.bayarCicilan(loan.getId(), loan.getTagihanBulanIni());
        
        // Cek tagihan bulan ini harusnya 0 setelah bayar
        assertEquals(0, BigDecimal.ZERO.compareTo(loan.getTagihanBulanIni().getAmount()));
        
        // Jalankan simulasi tenor berikutnya
        loanService.simulasiTenorBerikutnya(loan.getId());
        
        // Tagihan baru untuk bulan ke-2 harusnya terbentuk
        BigDecimal expectedBill = new BigDecimal("250000"); // pokok 1jt/5=200rb + bunga 1jt*5%=50rb = 250rb
        assertEquals(0, expectedBill.compareTo(loan.getTagihanBulanIni().getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    @Test
    void bayarCicilan_mendistribusikanCicilan_keSatuLender() throws Exception {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LenderRepository lenderRepository = mock(LenderRepository.class);
        LoanService loanService = new LoanService(loanRepository, borrowerRepository, lenderRepository, null, null);

        LenderId lenderId = new LenderId("LND-S01");
        Money investasi = new Money(new BigDecimal("1000000"), "IDR");

        Borrower borrower = new Borrower(new BorrowerId("BR-S01"), new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        borrower.tambahSaldo(new Money(new BigDecimal("500000"), "IDR")); // cukup untuk tagihan

        Loan loan = new Loan(new LoanId("LN-S01"), borrower.getId(), investasi, 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.tambahPendanaan(lenderId, investasi); // → auto FUNDING_READY
        loan.ubahStatus("DISBURSED");
        loan.generateMonthlyBill(); // tagihan = 250.000

        Lender lender = new Lender(lenderId, new Money(BigDecimal.ZERO, "IDR"));
        lender.setKycStatus(true);

        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(borrowerRepository.findById(borrower.getId())).thenReturn(borrower);
        when(lenderRepository.findById(lenderId)).thenReturn(lender);

        Money tagihan = loan.getTagihanBulanIni(); // 250.000
        loanService.bayarCicilan(loan.getId(), tagihan);

        // Satu-satunya lender → saldo harus bertambah sejumlah tagihan penuh
        assertEquals(0, tagihan.getAmount().compareTo(lender.getSaldoBalance().getAmount()));
    }
}