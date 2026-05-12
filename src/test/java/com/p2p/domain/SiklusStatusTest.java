package com.p2p.domain;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.loan.Loan;
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
            loan.ubahStatus("FUNDING");
        
            Money investasiAmount = new Money(new BigDecimal("100000"), "IDR");
            lender.kurangiSaldoUntukInvestasi(investasiAmount);
            loan.tambahPendanaan(lender.getId(), investasiAmount);
            
            // Cek apakah total terkumpul sudah mencapai target (menggunakan compareTo)
            if(loan.getTotalTerkumpul().getAmount().compareTo(loan.getTargetNominal().getAmount()) == 0){
                loan.ubahStatus("FUNDING_READY");
            }
            assertEquals("FUNDING_READY", loan.getStatus());
        }
}
