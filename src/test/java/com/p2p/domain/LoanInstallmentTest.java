package com.p2p.domain;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.strategy.FixedInterestStrategy;
import com.p2p.domain.loan.strategy.FloatingInterestStrategy;
import com.p2p.domain.loan.strategy.SyariahInterestStrategy;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class LoanInstallmentTest {

    // ======= TDD: FixedInterestStrategy =======

    @Test
    void fixedStrategy_hitungCicilan_hasilBenar() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        FixedInterestStrategy strategy = new FixedInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.hitungCicilan(target, target, 5);

        // pokok: 10jt/5 = 2jt, bunga: 10jt * 5% = 500rb, total = 2.5jt
        assertEquals(0, new BigDecimal("2500000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // ======= TDD: SyariahInterestStrategy =======

    @Test
    void syariahStrategy_hitungCicilan_hasilBenar() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        SyariahInterestStrategy strategy = new SyariahInterestStrategy(new BigDecimal("150000"));

        Money result = strategy.hitungCicilan(target, target, 5);

        // pokok: 10jt/5 = 2jt, margin flat: 150rb, total = 2.15jt
        assertEquals(0, new BigDecimal("2150000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    @Test
    void syariahStrategy_tidakTerpengaruhSisaPokok() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Money remaining = new Money(new BigDecimal("4000000"), "IDR");
        SyariahInterestStrategy strategy = new SyariahInterestStrategy(new BigDecimal("150000"));

        Money result = strategy.hitungCicilan(target, remaining, 5);

        // margin flat tetap 150rb meski sisa pokok berkurang — berbeda dari floating
        assertEquals(0, new BigDecimal("2150000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // ======= TDD: FloatingInterestStrategy =======

    @Test
    void floatingStrategy_cicilanBulanPertama_hasilBenar() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        FloatingInterestStrategy strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.hitungCicilan(target, target, 5);

        // pokok: 10jt/5 = 2jt, bunga: 10jt * 5% = 500rb, total = 2.5jt
        assertEquals(0, new BigDecimal("2500000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    @Test
    void floatingStrategy_cicilanBulanKedua_lebihKecil() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Money remaining = new Money(new BigDecimal("8000000"), "IDR");
        FloatingInterestStrategy strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.hitungCicilan(target, remaining, 5);

        // pokok: 10jt/5 = 2jt, bunga: 8jt * 5% = 400rb, total = 2.4jt
        assertEquals(0, new BigDecimal("2400000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // ======= TDD: Loan.bayarCicilan() =======

    @Test
    void payInstallment_sukses_billMenjadiNol() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-001"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill();

        loan.bayarCicilan(loan.getTagihanBulanIni());

        assertEquals(0, BigDecimal.ZERO.compareTo(loan.getTagihanBulanIni().getAmount()));
    }

    @Test
    void payInstallment_kurang_throwException() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-002"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill();

        Money tooSmall = new Money(new BigDecimal("1000"), "IDR");

        Exception ex = assertThrows(Exception.class, () -> loan.bayarCicilan(tooSmall));
        assertEquals("Nominal pembayaran kurang dari nominal tagihan", ex.getMessage());
    }

    @Test
    void payInstallment_sukses_sisaPokokBerkurang() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-003"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FloatingInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill();

        loan.bayarCicilan(loan.getTagihanBulanIni());
        loan.generateMonthlyBill();

        // bulan 2: sisa pokok 8jt, bunga 8jt*5%=400rb, total 2.4jt
        assertEquals(0, new BigDecimal("2400000").compareTo(
                loan.getTagihanBulanIni().getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // ======= TDD: isLunas() & status CLOSED =======

    @Test
    void isLunas_setelahSemuaCicilanDibayar_returnTrue() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-004"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));

        for (int i = 0; i < 5; i++) {
            loan.generateMonthlyBill();
            loan.bayarCicilan(loan.getTagihanBulanIni());
        }

        assertTrue(loan.isLunas());
    }

    @Test
    void statusClosed_setelahCicilanTerakhirDibayar() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-005"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));

        for (int i = 0; i < 5; i++) {
            loan.generateMonthlyBill();
            loan.bayarCicilan(loan.getTagihanBulanIni());
        }

        assertEquals("CLOSED", loan.getStatus());
    }

    @Test
    void isLunas_sebelumLunas_returnFalse() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-006"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));

        loan.generateMonthlyBill();
        loan.bayarCicilan(loan.getTagihanBulanIni());

        assertFalse(loan.isLunas());
    }

    // ======= TDD: OVERDUE - denda = sisaPokok * 2% =======

    @Test
    void bayarCicilan_statusOverdue_dendaTertrackDiOverdueFeesAccrued() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-O02"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("OVERDUE");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill();
        // Denda = sisaPokok(10jt) * 2% = 200.000
        // Tagihan = 2.500.000 + 200.000 = 2.700.000

        loan.bayarCicilan(loan.getTagihanBulanIni());

        // Denda 200.000 harus tercatat di overdueFeesAccrued
        assertEquals(0, new BigDecimal("200000").compareTo(
                loan.getTotalDendaTerkumpul().getAmount()));
    }

    @Test
    void hitungDistribusiCicilan_statusOverdue_dendaMasukKeDistribusiLender() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        LenderId lenderId = new LenderId("LND-O01");

        Loan loan = new Loan(new LoanId("LN-O01"), new BorrowerId("BR-001"), target, 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.tambahPendanaan(lenderId, target);
        loan.ubahStatus("OVERDUE");
        loan.generateMonthlyBill();
        // Tagihan = 2.500.000 + denda 200.000 = 2.700.000

        java.util.Map<LenderId, Money> distribusi = loan.hitungDistribusiCicilan();

        // Lender menerima tagihan PENUH termasuk denda (sesuai aturan baru)
        assertEquals(0, new BigDecimal("2700000").compareTo(
                distribusi.get(lenderId).getAmount().setScale(0, RoundingMode.HALF_UP)));
    }
}