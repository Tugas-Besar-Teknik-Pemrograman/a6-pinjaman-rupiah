package com.p2p.domain;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.strategy.FixedInterestStrategy;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.valueobject.Money;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SiklusStatusTest {

        private Borrower borrower;
        private Loan loan;
        private Lender lender;

        @BeforeEach
        public void setUp() {
            borrower = new Borrower("borrower1", new Money(new BigDecimal("0"), "IDR"));
            lender = new Lender("lender1", new Money(new BigDecimal("200000"), "IDR"));
            loan = new Loan("loan1", borrower.getId(), new Money(new BigDecimal("100000"), "IDR"));
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
            Money paymentAmount = new Money(new BigDecimal("13334"), "IDR");
            loan.payInstallment(paymentAmount);

            // Assert: status masih REPAYMENT sampai semua cicilan lunas
            assertEquals("REPAYMENT", loan.getStatus());
        }

        @Test
        public void Test5PembayaranCicilanTerakhir() throws Exception {
            loan.ubahStatus("REPAYMENT");
            assertEquals("REPAYMENT", loan.getStatus());

            if(loan.isLunas() == true){
                loan.ubahStatus("CLOSED");
                assertEquals("CLOSED", loan.getStatus());
            }
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
            
            loan.isPinjamanExpired();
            if(loan.isPinjamanExpired() == true){
                loan.ubahStatus("CANCELED");
            }

            assertEquals("CANCELED", loan.getStatus());
        }

        @Test
        public void Test8PinjamanOverdue() {
            loan.ubahStatus("DISBURSED");
            assertEquals("DISBURSED", loan.getStatus());

            loan.isPinjamanOverdue();
            if(loan.isPinjamanOverdue() == true){
                loan.ubahStatus("OVERDUE");
            }

            assertEquals("OVERDUE", loan.getStatus());
        }

        @Test
        public void Test9PinjamanMenjadiRepaymentLagi() {
            loan.ubahStatus("OVERDUE");
            assertEquals("OVERDUE", loan.getStatus());

            loan.isOverduePaid();
            if(loan.isOverduePaid() == true){
                loan.ubahStatus("REPAYMENT");
            }
            assertEquals("REPAYMENT", loan.getStatus());
        }
}
