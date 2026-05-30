package com.p2p.domain;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BorrowerTest {

    private Borrower borrower;

    @BeforeEach
    void inisialisasi() {
        borrower = new Borrower(new BorrowerId("B-001"), new Money(new BigDecimal("10000000"), "IDR"));
        borrower.setKycStatus(true);
        borrower.setCreditScore(700);
        borrower.setHasActiveLoan(false);
    }

    @Test
    void saldo_awal_borrower_nol() {
        assertEquals(0, BigDecimal.ZERO.compareTo(borrower.getSaldoBalance().getAmount()));
    }

    @Test
    void tambahSaldo_menambah_saldo_borrower() {
        borrower.tambahSaldo(new Money(new BigDecimal("250000"), "IDR"));
        assertEquals(0, new BigDecimal("250000").compareTo(borrower.getSaldoBalance().getAmount()));
    }

    @Test
    void kurangiSaldo_mengurangi_saldo_borrower() {
        borrower.tambahSaldo(new Money(new BigDecimal("250000"), "IDR"));
        borrower.kurangiSaldo(new Money(new BigDecimal("100000"), "IDR"));
        assertEquals(0, new BigDecimal("150000").compareTo(borrower.getSaldoBalance().getAmount()));
    }

    @Test
    void pengajuan_pinjaman_disetujui_dan_Loan_terbentuk() {
        // Arrange
        Money nominal = new Money(new BigDecimal("2000000"), "IDR");
        LoanId loanIdBaru = new LoanId("001");

        // Act
        Loan loanBaru = borrower.ajukanPinjaman(loanIdBaru,nominal, 12);

        // Assert
        assertNotNull(loanBaru, "Loan harus berhasil dibuat");
        assertEquals(loanIdBaru, loanBaru.getId());

        BigDecimal sisaLimitExpected = new BigDecimal("3000000.00");
        assertEquals(sisaLimitExpected, borrower.getLimitPinjaman().getAmount());

        assertTrue(borrower.hasActiveLoan(), "Borrower harus ditandai memiliki pinjaman aktif");
    }

    @Test
    void pengajuan_pinjaman_ditolak_jika_mengajukan_lebih_dari_satu_pinjaman(){
        Money nominalpinjaman1 = new Money(new BigDecimal("200000"), "IDR");
        borrower.ajukanPinjaman(new LoanId("001"),nominalpinjaman1, 12);

        Money nominalpinjaman2 = new Money(new BigDecimal("100000"),"IDR");

        IllegalStateException exception = assertThrows(IllegalStateException.class,()-> {
            borrower.ajukanPinjaman(new LoanId("002"), nominalpinjaman2, 6);
        });

        assertEquals("Lunasi Peminjaman sebelumnya dulu", exception.getMessage());
    }

    @Test
    void pengajuan_peminjaman_ditolak_jika_mengajukan_nominal_kurang_dari_minimum(){
        Money nominalpinjaman1 = new Money(new BigDecimal("-10"), "IDR");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,()-> {
            borrower.ajukanPinjaman(new LoanId("001"), nominalpinjaman1, 12);
        });

        assertEquals("Nominal pinjaman harus lebih dari 100.000", exception.getMessage());
    }

    @Test
    void pengajuan_peminjaman_ditolak_jika_kyc_belum_terverifikasi(){
        borrower.setKycStatus(false);
        Money nominalpinjaman = new Money(new BigDecimal("2000000"),"IDR");

        IllegalStateException exception = assertThrows(IllegalStateException.class, ()-> {
            borrower.ajukanPinjaman(new LoanId("003"),nominalpinjaman, 12);
        });

        assertEquals("Peminjaman ditolak karena Borrower belum terverifikasi (KYC)", exception.getMessage());

    }

    @Test
    void pengajuan_peminjaman_ditolak_jika_credit_score_rendah(){
        //ambang batas credit score = 600
        borrower.setCreditScore(300);
        Money nominal = new Money(new BigDecimal("2000000"), "IDR");

        IllegalStateException exception = assertThrows(IllegalStateException.class, ()->{
            borrower.ajukanPinjaman(new LoanId("004"),nominal,12);
        });

        assertEquals("Peminjaman ditolak karena Credit score di bawah ambang batas", exception.getMessage());
    }

    @Test
    void pengajuan_peminjaman_ditolak_jika_melebihi_limit_peminjaman() {
        //limit di set 10000000
        Money nominalLebih = new Money(new BigDecimal("15000000"), "IDR");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            borrower.ajukanPinjaman(new LoanId("005"), nominalLebih, 12);
        });

        assertEquals("Pengajuan melebihi limit, limit Anda adalah Rp 3000000.00", exception.getMessage());
    }

    @Test
    void pengajuan_peminjaman_disetujui_jika_kyc_sudah_terverifikasi(){
        borrower.setKycStatus(true);
        Money nominalpinjaman = new Money(new BigDecimal("2000000"),"IDR");
        LoanId loanIdBaru = new LoanId("003");

        Loan loanBaru = borrower.ajukanPinjaman(new LoanId("003"), nominalpinjaman, 12);

        assertNotNull(loanBaru, "Pinjaman harus disetujui dan objek Loan harus terbentuk");
        assertEquals(loanIdBaru, loanBaru.getId(), "ID Loan harus sama dengan yang diajukan");
        assertEquals("FUNDING", loanBaru.getStatus(), "Status awal pinjaman harus FUNDING");

        assertTrue(borrower.hasActiveLoan(), "Borrower harus ditandai memiliki pinjaman aktif");
    }

    @Test
    void pengajuan_peminjaman_berhasil_jika_credit_score_tinggi() {
        borrower.setKycStatus(true);
        borrower.setCreditScore(900);
        Money nominalPinjaman = new Money(new BigDecimal("200000"), "IDR"); // Pinjam 200 ribu
        LoanId loanIdBaru = new LoanId("LN-005");

        Loan loanBaru = borrower.ajukanPinjaman(loanIdBaru, nominalPinjaman, 12);

        assertNotNull(loanBaru, "Pinjaman harus disetujui");
        assertEquals("FUNDING", loanBaru.getStatus(), "Status pinjaman baru harus FUNDING");
        assertTrue(borrower.hasActiveLoan(), "Borrower harus ditandai memiliki pinjaman aktif");

        BigDecimal sisaLimitExpected = new BigDecimal("3000000.00");
        assertEquals(sisaLimitExpected, borrower.getLimitPinjaman().getAmount(), "Limit plafon tidak boleh berubah");
    }

    @Test
    void hitungLimitDenganTenorDanBunga_syariah_hasilBenar() {
        // Income = 10,000,000, Tenor = 12, Syariah (bunga 0%)
        // Batas Cicilan = 3,000,000
        // Limit = 3,000,000 / (1/12 + 0.0) = 36,000,000
        Money limit = borrower.hitungLimitDenganTenorDanBunga(12, "syariah");
        assertEquals(0, new BigDecimal("36000000.00").compareTo(limit.getAmount()));
    }

    @Test
    void hitungLimitDenganTenorDanBunga_flat_hasilBenar() {
        // Income = 10,000,000, Tenor = 12, Flat (bunga 5% = 0.05)
        // Batas Cicilan = 3,000,000
        // Limit = 3,000,000 / (1/12 + 0.05) = 22,500,000
        Money limit = borrower.hitungLimitDenganTenorDanBunga(12, "flat");
        assertEquals(0, new BigDecimal("22500000.00").compareTo(limit.getAmount()));
    }

    @Test
    void hitungLimitDenganTenorDanBunga_float_hasilBenar() {
        // Income = 10,000,000, Tenor = 12, Float (bunga 5% = 0.05)
        // Batas Cicilan = 3,000,000
        // Limit = 3,000,000 / (1/12 + 0.05) = 22,500,000
        Money limit = borrower.hitungLimitDenganTenorDanBunga(12, "float");
        assertEquals(0, new BigDecimal("22500000.00").compareTo(limit.getAmount()));
    }
}