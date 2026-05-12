package com.p2p.domain;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NotifikasiTest {
    private Loan loan;

    @BeforeEach
    void setUp() {
        loan = new Loan("L-001", "BR-001", new Money(new BigDecimal("10000000"), "IDR"));
    }

    @Test
    void loan_berstatus_DISBURSED_layak_notifikasi_berhasil() {
        Loan loan = new Loan("L-001", "BR-001", new Money(new BigDecimal("10000000"), "IDR"));
        loan.ubahStatus("DISBURSED");
        assertTrue(loan.isLayakNotifikasiPencairan());
    }

    @Test
    void loan_berstatus_FUNDING_tidak_layak_notifikasi_berhasil() {
        Loan loan = new Loan("L-001", "BR-001", new Money(new BigDecimal("10000000"), "IDR"));
        loan.ubahStatus("FUNDING");
        assertFalse(loan.isLayakNotifikasiPencairan());
    }

    @Test
    void loan_berstatus_FUNDING_READY_tidak_layak_notifikasi_berhasil() {
        Loan loan = new Loan("L-001", "BR-001", new Money(new BigDecimal("10000000"), "IDR"));
        loan.ubahStatus("FUNDING_READY");
        assertFalse(loan.isLayakNotifikasiPencairan());
    }

    @Test
    void loan_berstatus_REJECTED_tidak_layak_notifikasi_berhasil(){
        Loan loan = new Loan("L-001", "BR-001", new Money(new BigDecimal("10000000"), "IDR"));
        loan.ubahStatus("REJECTED");
        assertFalse(loan.isLayakNotifikasiPencairan());
    }
}