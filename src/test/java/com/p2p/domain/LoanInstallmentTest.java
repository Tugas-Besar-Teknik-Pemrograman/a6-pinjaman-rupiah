package com.p2p.domain;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.strategy.FixedInterestStrategy;
import com.p2p.domain.loan.strategy.FloatingInterestStrategy;
import com.p2p.domain.loan.strategy.SyariahInterestStrategy;
import com.p2p.domain.valueobject.Money;
import com.p2p.infrastructure.memory.InMemoryLoanRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoanInstallmentTest {

    //TDD: FixedInterestStrategy 

    @Test
    void fixedStrategy_hitungCicilan_hasilBenar() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        FixedInterestStrategy strategy = new FixedInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.hitungCicilan(target, target, 5);

        // pokok: 10jt/5 = 2jt, bunga: 10jt * 5% = 500rb, total = 2.5jt
        assertEquals(0, new BigDecimal("2500000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    //TDD: SyariahInterestStrategy 

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

    // TDD: FloatingInterestStrategy 

    @Test
    void floatingStrategy_cicilanBulanPertama_hasilBenar() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        FloatingInterestStrategy strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.hitungCicilan(target, target, 5);

        assertEquals(0, new BigDecimal("2500000").compareTo(
                result.getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    @Test
    void floatingStrategy_cicilanBulanKedua_lebihKecil() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Money remaining = new Money(new BigDecimal("8000000"), "IDR");
        FloatingInterestStrategy strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));

        Money result = strategy.hitungCicilan(target, remaining, 5);

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

        loan.bayarCicilan(loan.getTagihanBulanIni());

        assertEquals(0, BigDecimal.ZERO.compareTo(loan.getTagihanBulanIni().getAmount()));
    }

    @Test
    void cairkanPinjaman_menghasilkanTagihanAwal() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-001A"), new BorrowerId("BR-001"), target, 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.ubahStatus("FUNDING_READY");

        loan.cairkanPinjaman();

        assertEquals("DISBURSED", loan.getStatus());
        assertTrue(loan.getTagihanBulanIni().getAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void payInstallment_tagihanBelumTersedia_throwException() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-001B"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("DISBURSED");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));

        Exception ex = assertThrows(Exception.class, () -> loan.bayarCicilan(new Money(new BigDecimal("1"), "IDR")));
        assertEquals("Tagihan bulan ini belum tersedia", ex.getMessage());
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

    // TDD: isLunas() & status CLOSED 

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

        // baru bayar 1 dari 5 cicilan
        loan.generateMonthlyBill();
        loan.bayarCicilan(loan.getTagihanBulanIni());

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
        BigDecimal currentBillAmount = loan.getTagihanBulanIni().getAmount();
        
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
        loan.bayarCicilan(loan.getTagihanBulanIni());

        // Jika belum lunas total cicilan, status harusnya kembali normal (bukan OVERDUE lagi)
        assertEquals("REPAYMENT", loan.getStatus());
    }

    // TDD: Loan.hitungDistribusiCicilan()

    @Test
    void hitungDistribusiCicilan_satuLender_mendapatSemuaTagihan() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        LenderId lenderId = new LenderId("LND-D01");

        Loan loan = new Loan(new LoanId("LN-D01"), new BorrowerId("BR-001"), target, 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.tambahPendanaan(lenderId, target);  // → auto FUNDING_READY
        loan.ubahStatus("DISBURSED");            // override untuk test
        loan.generateMonthlyBill();              // tagihan = 2.500.000

        Map<LenderId, Money> distribusi = loan.hitungDistribusiCicilan();

        assertEquals(1, distribusi.size());
        assertEquals(0, new BigDecimal("2500000").compareTo(
            distribusi.get(lenderId).getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    @Test
    void hitungDistribusiCicilan_duaLender_proporsionalInvestasi() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        LenderId lenderA = new LenderId("LND-DA");
        LenderId lenderB = new LenderId("LND-DB");

        Loan loan = new Loan(new LoanId("LN-D02"), new BorrowerId("BR-001"), target, 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        // A: 6jt (60%), B: 4jt (40%) → total penuh → auto FUNDING_READY
        loan.tambahPendanaan(lenderA, new Money(new BigDecimal("6000000"), "IDR"));
        loan.tambahPendanaan(lenderB, new Money(new BigDecimal("4000000"), "IDR"));
        loan.ubahStatus("DISBURSED");
        loan.generateMonthlyBill(); // tagihan = 2.500.000

        Map<LenderId, Money> distribusi = loan.hitungDistribusiCicilan();

        // Total distribusi harus pas = tagihan (tidak ada yang hilang/lebih)
        BigDecimal totalDistribusi = distribusi.values().stream()
            .map(Money::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("2500000").compareTo(
            totalDistribusi.setScale(0, RoundingMode.HALF_UP)));

        // Lender A (60%) dapat Rp 1.500.000
        assertEquals(0, new BigDecimal("1500000").compareTo(
            distribusi.get(lenderA).getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    // TDD: LoanRepository.findByLenderId()

    @Test
    void bayarCicilan_statusOverdue_dendaTertrackDiOverdueFeesAccrued() throws Exception {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        Loan loan = new Loan(new LoanId("LN-O02"), new BorrowerId("BR-001"), target, 5);
        loan.ubahStatus("OVERDUE");
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.generateMonthlyBill(); // tagihan = 2.550.000

        loan.bayarCicilan(loan.getTagihanBulanIni());

        // Setelah bayar, denda Rp 50.000 harus tercatat di overdueFeesAccrued
        assertEquals(0, new BigDecimal("50000").compareTo(
            loan.getTotalDendaTerkumpul().getAmount()));
    }

    // TDD: LoanRepository.findByLenderId()

    @Test
    void findByLenderId_lenderAdaDiLoan_mengembalikanLoanTersebut() {
        LenderId lenderId = new LenderId("LND-P01");
        Money target = new Money(new BigDecimal("5000000"), "IDR");

        Loan loan = new Loan(new LoanId("LN-P01"), new BorrowerId("BR-001"), target, 6);
        loan.tambahPendanaan(lenderId, target); // → auto FUNDING_READY

        InMemoryLoanRepository repo = new InMemoryLoanRepository();
        repo.save(loan);

        List<Loan> hasil = repo.findByLenderId(lenderId);

        assertEquals(1, hasil.size());
        assertEquals("LN-P01", hasil.get(0).getId().getValue());
    }

    // TDD: Denda OVERDUE tidak masuk distribusi lender

    @Test
    void hitungDistribusiCicilan_statusOverdue_dendaTidakMasukKeDistribusiLender() {
        Money target = new Money(new BigDecimal("10000000"), "IDR");
        LenderId lenderId = new LenderId("LND-O01");

        Loan loan = new Loan(new LoanId("LN-O01"), new BorrowerId("BR-001"), target, 5);
        loan.setInterestStrategy(new FixedInterestStrategy(new BigDecimal("0.05")));
        loan.tambahPendanaan(lenderId, target); // full funding → FUNDING_READY
        loan.ubahStatus("OVERDUE");
        loan.generateMonthlyBill(); // tagihan = 2.500.000 + 50.000 denda = 2.550.000

        Map<LenderId, Money> distribusi = loan.hitungDistribusiCicilan();

        // Lender hanya dapat cicilan normal 2.500.000, BUKAN 2.550.000
        assertEquals(0, new BigDecimal("2500000").compareTo(
            distribusi.get(lenderId).getAmount().setScale(0, RoundingMode.HALF_UP)));
    }

    @Test
    void findByLenderId_lenderTidakAdaDiLoan_mengembalikanListKosong() {
        LenderId lenderAda = new LenderId("LND-P02");
        LenderId lenderTidakAda = new LenderId("LND-P03");
        Money target = new Money(new BigDecimal("5000000"), "IDR");

        Loan loan = new Loan(new LoanId("LN-P02"), new BorrowerId("BR-001"), target, 6);
        loan.tambahPendanaan(lenderAda, target);

        InMemoryLoanRepository repo = new InMemoryLoanRepository();
        repo.save(loan);

        List<Loan> hasil = repo.findByLenderId(lenderTidakAda);

        assertTrue(hasil.isEmpty());
    }
}