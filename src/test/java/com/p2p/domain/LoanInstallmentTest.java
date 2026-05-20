package com.p2p.domain;

import com.p2p.domain.borrower.BorrowerId;
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

    //TDD: FixedInterestStrategy 

    @Test
    void fixedStrategy_hitungCicilan_hasilBenar() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        FixedInterestStrategy strategy = new FixedInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.calculateInstallment(target, target, 5);

        // pokok: 10jt/5 = 2jt, bunga: 10jt * 5% = 500rb, total = 2.5jt
        assertEquals(0, new BigDecimal("2500000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    //TDD: SyariahInterestStrategy 

    @Test
    void syariahStrategy_hitungCicilan_hasilBenar() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        SyariahInterestStrategy strategy = new SyariahInterestStrategy(new BigDecimal("150000"));

        Money result = strategy.calculateInstallment(target, target, 5);

        // pokok: 10jt/5 = 2jt, margin flat: 150rb, total = 2.15jt
        assertEquals(0, new BigDecimal("2150000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    @Test
    void syariahStrategy_tidakTerpengaruhSisaPokok() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Money remaining = new Money(new BigDecimal("4000000"), "IDR");
        SyariahInterestStrategy strategy = new SyariahInterestStrategy(new BigDecimal("150000"));

        Money result = strategy.calculateInstallment(target, remaining, 5);

        // margin flat tetap 150rb meski sisa pokok berkurang — berbeda dari floating
        assertEquals(0, new BigDecimal("2150000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // TDD: FloatingInterestStrategy 

    @Test
    void floatingStrategy_cicilanBulanPertama_hasilBenar() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        FloatingInterestStrategy strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.calculateInstallment(target, target, 5);

        assertEquals(0, new BigDecimal("2500000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    @Test
    void floatingStrategy_cicilanBulanKedua_lebihKecil() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Money remaining = new Money(new BigDecimal("8000000"), "IDR");
        FloatingInterestStrategy strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.calculateInstallment(target, remaining, 5);

        assertEquals(0, new BigDecimal("2400000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // TDD: Loan.payInstallment() 

    @Test
    void payInstallment_sukses_billMenjadiNol() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-001"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill();

        loan.payInstallment(loan.getCurrentMonthBill());

        assertEquals(0, BigDecimal.ZERO.compareTo(loan.getCurrentMonthBill().getAmount()));
    }

    @Test
    void payInstallment_kurang_throwException() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-002"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill();

        Money tooSmall = new Money(new BigDecimal("1000"), "IDR");

        Exception ex = assertThrows(Exception.class, () -> loan.payInstallment(tooSmall));
        assertEquals("Nominal pembayaran kurang dari nominal tagihan", ex.getMessage());
    }

    @Test
    void payInstallment_sukses_sisaPokokBerkurang() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-003"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FloatingInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill();

        loan.payInstallment(loan.getCurrentMonthBill());
        loan.generateMonthlyBill();

        // bulan 2: sisa pokok 8jt, bunga 8jt*5%=400rb, total 2.4jt
        assertEquals(0, new BigDecimal("2400000").compareTo(
                loan.getCurrentMonthBill().getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // TDD: isLunas() & status CLOSED 

    @Test
    void isLunas_setelahSemuaCicilanDibayar_returnTrue() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-004"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));

        for (int i = 0; i < 5; i++) {
            loan.generateMonthlyBill();
            loan.payInstallment(loan.getCurrentMonthBill());
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
            loan.payInstallment(loan.getCurrentMonthBill());
        }

        assertEquals("CLOSED", loan.getStatus());
    }

    @Test
    void isLunas_sebelumLunas_returnFalse() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-006"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));

        // baru bayar 1 dari 5 cicilan
        loan.generateMonthlyBill();
        loan.payInstallment(loan.getCurrentMonthBill());

        assertFalse(loan.isLunas());
    }

    @Test
    void getSisaTagihanKeseluruhan_sebelumAdaPembayaran_samaDenganTarget() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-007"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        
        // Asumsi: Sisa tagihan keseluruhan awal adalah target pokok (jika belum memperhitungkan bunga total di depan)
        assertEquals(0, new BigDecimal("10000000").compareTo(
                loan.getSisaTagihanKeseluruhan().getAmount()));
    }

    @Test
    void generateMonthlyBill_statusOverdue_tagihanBertambahDenda() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-008"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("OVERDUE"); // Status dibuat telat bayar
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        
        loan.generateMonthlyBill();
        
        // Tagihan normal (berdasarkan test case atas) = 2.500.000
        // Jika ada denda (misal flat 50.000 atau percentage), maka harus > 2.500.000
        BigDecimal normalBill = new BigDecimal("2500000");
        BigDecimal currentBillAmount = loan.getCurrentMonthBill().getAmount();
        
        assertTrue(currentBillAmount.compareTo(normalBill) > 0, 
            "Tagihan " + currentBillAmount + " seharusnya > " + normalBill + " karena ada denda overdue");
    }

    @Test
    void payInstallment_statusOverdue_lunas_statusKembaliKeDisbursed() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-009"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("OVERDUE"); 
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill();
        
        // Bayar lunas tagihan bulan ini (termasuk denda)
        loan.payInstallment(loan.getCurrentMonthBill());
        
        // Jika belum lunas total cicilan, status harusnya kembali normal (bukan OVERDUE lagi)
        assertEquals("REPAYMENT", loan.getStatus()); 
    }
}