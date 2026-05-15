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

import static org.junit.jupiter.api.Assertions.*;

class LoanInstallmentTest {

    //TDD: FixedInterestStrategy

    @Test
    void fixedStrategy_hitungCicilan_hasilBenar() {
        
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        FixedInterestStrategy strategy = new FixedInterestStrategy(new BigDecimal("0.05"));

        
        Money result = strategy.calculateInstallment(target, target, 5);

        
        assertEquals(0, new BigDecimal("2500000").compareTo(result.getAmount().setScale(0, java.math.RoundingMode.HALF_UP)));
    }

    //TDD: SyariahInterestStrategy 

    @Test
    void syariahStrategy_hitungCicilan_hasilBenar() {
        
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        SyariahInterestStrategy strategy = new SyariahInterestStrategy(new BigDecimal("150000"));

        
        Money result = strategy.calculateInstallment(target, target, 5);

        
        assertEquals(0, new BigDecimal("2150000").compareTo(result.getAmount().setScale(0, java.math.RoundingMode.HALF_UP)));
    }

    //TDD: FloatingInterestStrategy 

    @Test
    void floatingStrategy_cicilanBulanPertama_hasilBenar() {
        
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        FloatingInterestStrategy strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));

        
        Money result = strategy.calculateInstallment(target, target, 5);

        
        assertEquals(0, new BigDecimal("2500000").compareTo(result.getAmount().setScale(0, java.math.RoundingMode.HALF_UP)));
    }

    @Test
    void floatingStrategy_cicilanBulanKedua_lebihKecil() {
        
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        
        Money remaining = new Money(new BigDecimal("8000000"), "IDR");
        FloatingInterestStrategy strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));

        
        Money result = strategy.calculateInstallment(target, remaining, 5);

        
        assertEquals(0, new BigDecimal("2400000").compareTo(result.getAmount().setScale(0, java.math.RoundingMode.HALF_UP)));
    }

    //TDD: Loan.payInstallment() 

    @Test
    void payInstallment_sukses_billMenjadiNol() throws Exception {
        
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-001"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill(); // bill = 2.5jt

       
        loan.payInstallment(loan.getCurrentMonthBill());

        
        assertEquals(0, BigDecimal.ZERO.compareTo(loan.getCurrentMonthBill().getAmount()));
    }

    @Test
    void payInstallment_kurang_throwException() {
        
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-002"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill(); // bill = 2.5jt

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
        loan.generateMonthlyBill(); // bulan 1: 2.5jt

        loan.payInstallment(loan.getCurrentMonthBill());
        loan.generateMonthlyBill(); // bulan 2: harusnya lebih kecil karena floating

        
        assertEquals(0, new BigDecimal("2400000").compareTo(loan.getCurrentMonthBill().getAmount().setScale(0, java.math.RoundingMode.HALF_UP)));
    }
}