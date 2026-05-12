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
    void pengajuan_pinjaman_disetujui_Loan_terbentuk() {
        // Arrange
        Money nominal = new Money(new BigDecimal("2000000"), "IDR");

        // Act
        Loan loanBaru = borrower.ajukanPinjaman("001",nominal, 12);

        // Assert
        assertNotNull(loanBaru, "Loan harus berhasil dibuat");
        assertEquals("001", loanBaru.getId());

        BigDecimal sisaLimitExpected = new BigDecimal("10000000");
        assertEquals(sisaLimitExpected, borrower.getLimitPinjaman().getAmount());

        assertTrue(borrower.hasActiveLoan(), "Borrower harus ditandai memiliki pinjaman aktif");
    }

    @Test
    void pengajuan_pinjaman_ditolak_jika_mengajukan_lebih_dari_satu(){
        Money nominalpinjaman1 = new Money(new BigDecimal("200000"), "IDR");
        borrower.ajukanPinjaman("001",nominalpinjaman1, 12);

        Money nominalpinjaman2 = new Money(new BigDecimal("100000"),"IDR");

        IllegalStateException exception = assertThrows(IllegalStateException.class,()-> {
            borrower.ajukanPinjaman("002", nominalpinjaman2, 6);
        });

        assertEquals("Lunasi Peminjaman sebelumnya dulu", exception.getMessage());
    }

    @Test
    void pengajuan_peminjaman_ditolak_jika_mengajukan_nominal_negatif_atau_nol(){
        Money nominalpinjaman1 = new Money(new BigDecimal("-10"), "IDR");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,()-> {
            borrower.ajukanPinjaman("001", nominalpinjaman1, 12);
        });

        assertEquals("Nominal pinjaman harus lebih dari 0", exception.getMessage());
    }

    @Test
    void pengajuan_peminjaman_ditolak_jika_kyc_belum_terverifikasi(){
        borrower.setKycStatus(false);
        Money nominalpinjaman = new Money(new BigDecimal("2000000"),"IDR");

        IllegalStateException exception = assertThrows(IllegalStateException.class, ()-> {
            borrower.ajukanPinjaman("003",nominalpinjaman, 12);
        });

        assertEquals("Peminjaman ditolak karena Borrower belum terverifikasi (KYC)", exception.getMessage());

    }
}