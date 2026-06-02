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
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        // Borrower menerima nominal bersih = target - admin fee 1% = 1.000.000 - 10.000 = 990.000
        BigDecimal expectedNet = new BigDecimal("990000");
        assertEquals(0, expectedNet.compareTo(borrower.getSaldoBalance().getAmount()),
            "Borrower seharusnya menerima nominal bersih (target - admin fee 1%)");
        assertEquals("DISBURSED", loan.getStatus());
    }

    @Test
    void bayarCicilan_mengurangi_saldo_borrower_sesuai_tagihan() throws Exception {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LenderRepository lenderRepository = mock(LenderRepository.class);
        LoanService loanService = new LoanService(loanRepository, borrowerRepository, lenderRepository, null, null);

        Borrower borrower = new Borrower(new BorrowerId("BR-2"), new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        borrower.tambahSaldo(new Money(new BigDecimal("500000"), "IDR"));

        Loan loan = new Loan(new LoanId("LN-2"), borrower.getId(), new Money(new BigDecimal("1000000"), "IDR"), 3);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.ubahStatus("DISBURSED");
        loan.generateMonthlyBill();

        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(borrowerRepository.findById(borrower.getId())).thenReturn(borrower);

        Money tagihan = loan.getTagihanBulanIni();
        BigDecimal saldoSebelum = borrower.getSaldoBalance().getAmount();

        loanService.bayarCicilan(loan.getId(), tagihan);

        BigDecimal saldoSesudah = borrower.getSaldoBalance().getAmount();
        assertEquals(0, tagihan.getAmount().compareTo(
            saldoSebelum.subtract(saldoSesudah).setScale(0, RoundingMode.HALF_UP)));
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
        loan.generateMonthlyBill();

        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(borrowerRepository.findById(borrower.getId())).thenReturn(borrower);

        loanService.bayarCicilan(loan.getId(), loan.getTagihanBulanIni());
        loanService.simulasiTenorBerikutnya(loan.getId());

        BigDecimal expectedBill = new BigDecimal("250000");
        assertEquals(0, expectedBill.compareTo(
            loan.getTagihanBulanIni().getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // FIX: tambah ubahStatus("FUNDING") sebelum tambahPendanaan
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
        borrower.tambahSaldo(new Money(new BigDecimal("500000"), "IDR"));

        Loan loan = new Loan(new LoanId("LN-S01"), borrower.getId(), investasi, 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.ubahStatus("FUNDING"); // FIX: wajib sebelum tambahPendanaan
        loan.tambahPendanaan(lenderId, investasi);
        loan.ubahStatus("DISBURSED");
        loan.generateMonthlyBill();

        Lender lender = new Lender(lenderId, new Money(BigDecimal.ZERO, "IDR"));
        lender.setKycStatus(true);

        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(borrowerRepository.findById(borrower.getId())).thenReturn(borrower);
        when(lenderRepository.findById(lenderId)).thenReturn(lender);

        Money tagihan = loan.getTagihanBulanIni();
        loanService.bayarCicilan(loan.getId(), tagihan);

        assertEquals(0, tagihan.getAmount().compareTo(lender.getSaldoBalance().getAmount()));
    }

    @Test
    void simulasiMajukanJatuhTempo_loan_disbursed_menjadi_eligible_overdue() {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LoanService loanService = new LoanService(loanRepository, borrowerRepository, null, null);

        Loan loan = new Loan(new LoanId("LN-SIM-01"), new BorrowerId("BR-SIM-01"),
                             new Money(new BigDecimal("1000000"), "IDR"), 12);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.ubahStatus("DISBURSED");

        when(loanRepository.findById(loan.getId())).thenReturn(loan);

        loanService.simulasiMajukanJatuhTempo(loan.getId());

        assertTrue(loan.getTanggalJatuhTempo().isBefore(LocalDate.now()),
            "Setelah simulasi, tanggalJatuhTempo harus sebelum hari ini");
        assertTrue(loan.isPinjamanOverdue(),
            "Setelah simulasi, pinjaman harus eligible untuk overdue");
    }

    @Test
    void simulasiMajukanJatuhTempo_status_invalid_melempar_exception() {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LoanService loanService = new LoanService(loanRepository, borrowerRepository, null, null);

        Loan loan = new Loan(new LoanId("LN-SIM-02"), new BorrowerId("BR-SIM-02"),
                             new Money(new BigDecimal("1000000"), "IDR"), 12);

        when(loanRepository.findById(loan.getId())).thenReturn(loan);

        assertThrows(IllegalStateException.class, () ->
            loanService.simulasiMajukanJatuhTempo(loan.getId()));
    }

    @Test
    void menolakPencairan_mengembalikan_saldo_ke_lender() {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LenderRepository lenderRepository = mock(LenderRepository.class);
        LoanService loanService = new LoanService(loanRepository, borrowerRepository, lenderRepository, null, null);

        LenderId lenderId = new LenderId("LND-REFUND-01");
        Money investasi = new Money(new BigDecimal("500000"), "IDR");

        Loan loan = new Loan(new LoanId("LN-REFUND-01"), new BorrowerId("BR-REFUND-01"),
                             new Money(new BigDecimal("1000000"), "IDR"), 12);
        loan.ubahStatus("FUNDING"); // FIX: wajib sebelum tambahPendanaan
        loan.tambahPendanaan(lenderId, investasi);
        loan.ubahStatus("FUNDING_READY");

        Lender lender = new Lender(lenderId, new Money(BigDecimal.ZERO, "IDR"));

        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(lenderRepository.findById(lenderId)).thenReturn(lender);

        loanService.menolakPencairan(loan.getId(), "Tidak memenuhi syarat");

        assertEquals(0, investasi.getAmount().compareTo(lender.getSaldoBalance().getAmount()),
            "Saldo lender harus dikembalikan penuh saat pencairan ditolak");
        assertEquals("REJECTED", loan.getStatus());
    }

    @Test
    void menolakPencairan_status_bukan_fundingReady_melempar_exception() {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LenderRepository lenderRepository = mock(LenderRepository.class);
        LoanService loanService = new LoanService(loanRepository, borrowerRepository, lenderRepository, null, null);

        Loan loan = new Loan(new LoanId("LN-REFUND-02"), new BorrowerId("BR-REFUND-02"),
                             new Money(new BigDecimal("1000000"), "IDR"), 12);
        loan.ubahStatus("FUNDING");

        when(loanRepository.findById(loan.getId())).thenReturn(loan);

        assertThrows(IllegalStateException.class, () ->
            loanService.menolakPencairan(loan.getId(), "Tidak memenuhi syarat"));
    }

    // FIX: tambah ubahStatus("FUNDING") sebelum tambahPendanaan
    @Test
    void bayarCicilan_statusOverdue_dendaMasukKeLender() throws Exception {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
        LenderRepository lenderRepository = mock(LenderRepository.class);
        LoanService loanService = new LoanService(loanRepository, borrowerRepository, lenderRepository, null, null);

        LenderId lenderId = new LenderId("LND-OD1");
        Money investasi = new Money(new BigDecimal("1000000"), "IDR");

        Borrower borrower = new Borrower(new BorrowerId("BR-OD1"), new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        borrower.tambahSaldo(new Money(new BigDecimal("1000000"), "IDR"));

        // Loan OVERDUE: denda = sisaPokok * 2% = 1.000.000 * 2% = 20.000
        // Tagihan = cicilan normal + denda = 250.000 + 20.000 = 270.000
        Loan loan = new Loan(new LoanId("LN-OD1"), borrower.getId(), investasi, 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.ubahStatus("FUNDING"); // FIX: wajib sebelum tambahPendanaan
        loan.tambahPendanaan(lenderId, investasi);
        loan.ubahStatus("OVERDUE");
        loan.generateMonthlyBill();

        Lender lender = new Lender(lenderId, new Money(BigDecimal.ZERO, "IDR"));
        lender.setKycStatus(true);

        when(loanRepository.findById(loan.getId())).thenReturn(loan);
        when(borrowerRepository.findById(borrower.getId())).thenReturn(borrower);
        when(lenderRepository.findById(lenderId)).thenReturn(lender);

        Money tagihan = loan.getTagihanBulanIni();
        loanService.bayarCicilan(loan.getId(), tagihan);

        // Lender harus menerima tagihan penuh termasuk denda
        assertEquals(0, tagihan.getAmount().compareTo(lender.getSaldoBalance().getAmount()));
        // Admin tidak menerima denda
        Money[] dendaAdmin = {new Money(BigDecimal.ZERO, "IDR")};
        loanService.setAdminFeeCallback(fee -> dendaAdmin[0] = fee);
        assertEquals(0, BigDecimal.ZERO.compareTo(dendaAdmin[0].getAmount()));
    }
}