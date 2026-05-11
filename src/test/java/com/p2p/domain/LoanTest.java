package com.p2p.domain;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanTest {
    @Test
    void Peminjaman_gagal_kalo_lebih_dari_limit() {
        Money limit = new Money(new BigDecimal("500000"), "IDR");
        Borrower borrower = new Borrower("B-001", limit);

        Money pinjaman = new Money(new BigDecimal("600000"), "IDR");

        assertThrows(IllegalArgumentException.class,
                () -> borrower.BandingkanLimit(pinjaman));
    }

    @Test
    void Peminjaman_berhasil_kalo_kurangsamadengan_dari_limit() {
        Money limit = new Money(new BigDecimal("500000"), "IDR");
        Borrower borrower = new Borrower("B-001", limit);

        Money pinjaman = new Money(new BigDecimal("200000"), "IDR");

        borrower.BandingkanLimit(pinjaman);

        assertEquals(new BigDecimal("300000"), borrower.getLimitPinjaman().getAmount());
    }
}