package com.p2p.domain;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.strategy.FixedInterestStrategy;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.valueobject.Money;
import com.p2p.domain.state.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SiklusStatusTest {

        private Borrower borrower;
        private Loan loan;
        private Lender lender;

        @BeforeEach
        public void setUp() {
            borrower = new Borrower(new BorrowerId("borrower1"), new Money(new BigDecimal("0"), "IDR"));
            lender = new Lender(new LenderId("lender1"), new Money(new BigDecimal("200000"), "IDR"));
            loan = new Loan(new LoanId("loan1"), borrower.getId(), new Money(new BigDecimal("100000"), "IDR"));
            loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
            borrower.setKycStatus(true);
        }

        // 1. Borrower mengajukan pinjaman
        @Test
        public void TestBorrowerMengajukanPinjaman() {
            loan.ubahStatus("FUNDING");
            assertEquals("FUNDING", loan.getStatus());
        }

        @Test
        public void Test2PinjamanDidanai() throws Exception {
            // Arrange: pastikan loan di status FUNDING
            loan.ubahStatus("FUNDING");
            assertEquals("FUNDING", loan.getStatus());

            Money investasiAmount = new Money(new BigDecimal("100000"), "IDR");

            // Act: lender melakukan investasi penuh
            lender.kurangiSaldoUntukInvestasi(investasiAmount);
            loan.tambahPendanaan(lender.getId(), investasiAmount);

            // Assert: total terkumpul sama dengan target dan status berubah menjadi FUNDING_READY
            int compare = loan.getTotalTerkumpul().getAmount().compareTo(loan.getTargetNominal().getAmount());
            assertEquals(0, compare);
            assertEquals("FUNDING_READY", loan.getStatus());
        }

        
        @Test
        public void Test3PencairanDanaPinjaman(){
            // Arrange: pastikan loan di status FUNDING_READY
            loan.ubahStatus("FUNDING_READY");
            assertEquals("FUNDING_READY", loan.getStatus());

            // Act: lakukan pencairan dana (diasumsikan ada method/logika di Loan)
            // Untuk sekarang, ubah status langsung ke DISBURSED
            loan.ubahStatus("DISBURSED");

            // Assert: status berubah menjadi DISBURSED
            assertEquals("DISBURSED", loan.getStatus());
        }

        @Test
        public void Test4PembayaranPinjaman() throws Exception {
            // Arrange: pastikan loan di status DISBURSED dan siap angsuran
            loan.ubahStatus("DISBURSED");
            assertEquals("DISBURSED", loan.getStatus());

            // Simulate: generate tagihan bulanan (ini yang memicu awal REPAYMENT)
            loan.generateMonthlyBill();
            
            // Act: borrower melakukan pembayaran angsuran pertama
            Money paymentAmount = new Money(new BigDecimal("13333"), "IDR");
            loan.bayarCicilan(paymentAmount);

            // Assert: status masih REPAYMENT sampai semua cicilan lunas
            assertEquals("REPAYMENT", loan.getStatus());
        }

        @Test
        public void Test5PembayaranCicilanTerakhir() throws Exception {
            // Pakai tenor 1 agar satu kali bayar langsung lunas
            Loan loanSatuBulan = new Loan(
                new LoanId("loan-1bulan"),
                borrower.getId(),
                new Money(new BigDecimal("100000"), "IDR"),
                1
            );
            loanSatuBulan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
            loanSatuBulan.ubahStatus("DISBURSED");
            loanSatuBulan.setTanggalJatuhTempo(LocalDate.now().plusDays(30));
    
            loanSatuBulan.generateMonthlyBill();
            Money paymentAmount = new Money(new BigDecimal("105000"), "IDR");
            loanSatuBulan.bayarCicilan(paymentAmount);
 
        assertEquals("CLOSED", loanSatuBulan.getStatus());
        }

        @Test
        public void Test6PeminjamanDitolak() {

            borrower.setKycStatus(false);
            assertEquals(false, borrower.isKycStatus());

            loan.ubahStatus("REJECTED");
            assertEquals("REJECTED", loan.getStatus());

        }

        @Test
        public void Test7PeminjamanDibatalkan() {
            loan.ubahStatus("FUNDING");
            // Inject tanggal kadaluarsa yang sudah lewat kemarin
            loan.setTanggalKadaluarsaFunding(LocalDate.now().minusDays(1));
    
            assertTrue(loan.isPinjamanExpired(), "Seharusnya expired karena batas waktu sudah lewat");
    
            // CancelledState akan validasi isPinjamanExpired() sebelum ubah status
            assertDoesNotThrow(() -> {
                new com.p2p.domain.state.CanceledState().ubahStatus(loan);
            });
            assertEquals("CANCELED", loan.getStatus());
        }

        @Test
        public void Test8PinjamanOverdue() {
            loan.ubahStatus("REPAYMENT");
            // Inject tanggal jatuh tempo yang sudah lewat kemarin
            loan.setTanggalJatuhTempo(LocalDate.now().minusDays(1));
    
            assertTrue(loan.isPinjamanOverdue(),
                "Seharusnya overdue karena tanggal jatuh tempo sudah lewat");
    
            // OverdueState akan validasi isPinjamanOverdue() sebelum ubah status
            assertDoesNotThrow(() -> {
                new com.p2p.domain.state.OverdueState().ubahStatus(loan);
            });
            assertEquals("OVERDUE", loan.getStatus());
        }

        @Test
        public void Test9PinjamanMenjadiRepaymentLagi() throws Exception {
        loan.ubahStatus("OVERDUE");
        loan.setTanggalJatuhTempo(LocalDate.now().minusDays(1));
 
        // Generate tagihan dan bayar
        loan.generateMonthlyBill();
        Money paymentAmount = new Money(new BigDecimal("15333"), "IDR");
        loan.bayarCicilan(paymentAmount);
 
        // Setelah bayar, currentMonthBill = 0 → isOverduePaid() = true
        // payInstallment sudah trigger repayment() untuk status OVERDUE
        assertEquals("REPAYMENT", loan.getStatus());
    }
}
