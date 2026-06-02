package com.p2p.domain.borrower;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.valueobject.Money;
import com.p2p.domain.state.LoanStateFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Borrower {
    private static final int AMBANG_BATAS = 600;
    private static final BigDecimal MINIMAL_PEMINJAMAN = new BigDecimal("100000");
    private static final BigDecimal PERSENTASE_LIMIT = new BigDecimal("0.30");
    private static final BigDecimal MINIMAL_WITHDRAWAL = new BigDecimal("100000");
    private static final String INTEREST_FLAT = "flat";
    private static final String INTEREST_FIXED = "fixed";
    private static final String INTEREST_FLOAT = "float";
    private static final String INTEREST_FLOATING = "floating";
    private static final String INTEREST_SYARIAH = "syariah";
    private BorrowerId id;
    private Money limitPinjaman;
    private boolean kycStatus;
    private int creditScore;
    private boolean hasActiveLoan;
    private Money penghasilan;
    private Money saldoBalance;

    public Borrower(BorrowerId id, Money penghasilan) {
        this.id = id;
        this.penghasilan = penghasilan;
        this.limitPinjaman = hitungLimitMaksimal(penghasilan);
        this.kycStatus = false;
        this.creditScore = 0;
        this.hasActiveLoan = false;
        this.saldoBalance = new Money(BigDecimal.ZERO, penghasilan.getCurrency());
    }

    private Money hitungLimitMaksimal(Money penghasilan) {
        BigDecimal kalkulasi = penghasilan.getAmount().multiply(PERSENTASE_LIMIT);
        return new Money(kalkulasi, penghasilan.getCurrency());
    }

    public Money getPenghasilan() {
        return this.penghasilan;
    }

    public void setPenghasilan(Money penghasilan) {
        this.penghasilan = penghasilan;
    }

    public void tambahSaldo(Money nominalTambah) {
        validatePositiveAmount(nominalTambah);
        updateSaldo(nominalTambah.getAmount());
    }

    public void kurangiSaldo(Money nominalKurang) {
        validatePositiveAmount(nominalKurang);
        validateSufficientSaldo(nominalKurang);
        updateSaldo(nominalKurang.getAmount().negate());
    }

    public void tarikSaldo(Money nominalTarik) {
        this.validateKycStatusForWithdrawal();
        this.validateMinimalWithdrawal(nominalTarik);
        this.validateSufficientBalanceForWithdrawal(nominalTarik);
        this.updateSaldo(nominalTarik.getAmount().negate());
    }

    private void validateKycStatusForWithdrawal() {
        if (!this.kycStatus) {
            throw new IllegalStateException("Borrower tidak terverifikasi (KYC = false)");
        }
    }

    private void validateMinimalWithdrawal(Money nominalTarik) {
        if (nominalTarik.getAmount().compareTo(MINIMAL_WITHDRAWAL) < 0) {
            throw new IllegalArgumentException("Nominal penarikan minimal harus " + MINIMAL_WITHDRAWAL);
        }
    }

    private void validateSufficientBalanceForWithdrawal(Money nominalTarik) {
        if (this.saldoBalance.getAmount().compareTo(nominalTarik.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo tidak mencukupi untuk melakukan penarikan");
        }
    }

    public Money getSaldoBalance() {
        return this.saldoBalance;
    }

    private void validatePositiveAmount(Money nominal) {
        if (nominal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal harus lebih dari 0");
        }
    }

    private void validateSufficientSaldo(Money nominalKurang) {
        if (this.saldoBalance.getAmount().compareTo(nominalKurang.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo borrower tidak mencukupi");
        }
    }

    private void updateSaldo(BigDecimal amount) {
        BigDecimal newBalance = this.saldoBalance.getAmount().add(amount);
        this.saldoBalance = new Money(newBalance, this.saldoBalance.getCurrency());
    }

    public Money hitungLimitDenganTenorDanBunga(int tenor, String interestType) {
        BigDecimal rate = hitungRate(interestType, null);
        return hitungLimitDenganTenorDanBunga(tenor, rate);
    }

    private BigDecimal hitungRate(String interestType, BigDecimal customRateOrMargin) {
        if (INTEREST_FLAT.equalsIgnoreCase(interestType) || INTEREST_FIXED.equalsIgnoreCase(interestType)) {
            return (customRateOrMargin != null) ? customRateOrMargin : new BigDecimal("0.05");
        }
        if (INTEREST_FLOAT.equalsIgnoreCase(interestType) || INTEREST_FLOATING.equalsIgnoreCase(interestType)) {
            return (customRateOrMargin != null) ? customRateOrMargin : new BigDecimal("0.05");
        }
        return BigDecimal.ZERO;
    }

    public Money hitungLimitDenganTenorDanBunga(int tenor, BigDecimal bungaBulanan) {
        if (this.penghasilan == null) {
            return this.limitPinjaman;
        }
        if (tenor <= 0) {
            throw new IllegalArgumentException("Tenor harus lebih besar dari 0");
        }
        BigDecimal batasCicilan = this.penghasilan.getAmount().multiply(PERSENTASE_LIMIT);
        
        // Limit = (Batas Cicilan * Tenor) / (1 + Bunga Bulanan * Tenor)
        BigDecimal numerator = batasCicilan.multiply(BigDecimal.valueOf(tenor));
        BigDecimal tenorBunga = bungaBulanan.multiply(BigDecimal.valueOf(tenor));
        BigDecimal denominator = BigDecimal.ONE.add(tenorBunga);
        
        BigDecimal limit = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        return new Money(limit, this.penghasilan.getCurrency());
    }

    public Loan ajukanPinjaman(LoanId loanid, Money nominal, int tenor){
        validasiPinjaman(nominal);
        this.hasActiveLoan = true;
        Loan loan = new Loan(loanid, this.id, nominal, tenor);
        loan.setInterestStrategy(new com.p2p.domain.loan.strategy.FixedInterestStrategy(new BigDecimal("0.05")));
        loan.setJenisBunga(INTEREST_FLAT);
        LoanStateFactory.pendingToFunding().ubahStatus(loan);
        return loan;
    }

    public Loan ajukanPinjaman(LoanId loanid, Money nominal, int tenor, String interestType) {
        return ajukanPinjaman(loanid, nominal, tenor, interestType, null);
    }

    public Loan ajukanPinjaman(LoanId loanid, Money nominal, int tenor, String interestType, BigDecimal customRateOrMargin) {
        BigDecimal rate = hitungRate(interestType, customRateOrMargin);

        // Hitung limit secara dinamis berdasarkan Tenor dan Bunga
        this.limitPinjaman = hitungLimitDenganTenorDanBunga(tenor, rate);

        validasiPinjaman(nominal);
        this.hasActiveLoan = true;

        Loan loan = new Loan(loanid, this.id, nominal, tenor);

        // Set strategy pada loan
        var strategy = dapatkanInterestStrategy(interestType, rate, customRateOrMargin);
        loan.setInterestStrategy(strategy);

        loan.setJenisBunga(interestType.toLowerCase());
        LoanStateFactory.pendingToFunding().ubahStatus(loan);
        return loan;
    }

    private com.p2p.domain.loan.strategy.InterestCalculationStrategy dapatkanInterestStrategy(
            String interestType, BigDecimal rate, BigDecimal customRateOrMargin) {
        if (INTEREST_FLAT.equalsIgnoreCase(interestType) || INTEREST_FIXED.equalsIgnoreCase(interestType)) {
            return new com.p2p.domain.loan.strategy.FixedInterestStrategy(rate);
        }
        if (INTEREST_FLOAT.equalsIgnoreCase(interestType) || INTEREST_FLOATING.equalsIgnoreCase(interestType)) {
            return new com.p2p.domain.loan.strategy.FloatingInterestStrategy(rate);
        }
        if (INTEREST_SYARIAH.equalsIgnoreCase(interestType)) {
            BigDecimal margin = (customRateOrMargin != null) ? customRateOrMargin : new BigDecimal("150000");
            return new com.p2p.domain.loan.strategy.SyariahInterestStrategy(margin);
        }
        return null;
    }

    private void validasiPinjaman(Money nominal){
        //validasi KYC
        if(!this.kycStatus){
            throw new IllegalStateException("Peminjaman ditolak karena Borrower belum terverifikasi (KYC)");
        }

        //validasi credit score
        if(this.creditScore < AMBANG_BATAS){
            throw new IllegalStateException("Peminjaman ditolak karena Credit score di bawah ambang batas");
        }

        //validasi nominal gaboleh kurang dari minimum peminjaman
        if (nominal.getAmount().compareTo(MINIMAL_PEMINJAMAN) < 0) {
            throw new IllegalArgumentException("Nominal pinjaman harus lebih dari 100.000");
        }

        //validasi nominal harus kelipatan 100.000
        if (nominal.getAmount().remainder(MINIMAL_PEMINJAMAN).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Nominal pinjaman harus kelipatan 100.000");
        }

        //validasi nominal <= limit
        if (this.limitPinjaman.isLessThan(nominal)) {
            // Jika sisa limit lebih kecil dari yang mau dipinjam, ditolak
            throw new IllegalStateException("Pengajuan melebihi limit, limit Anda adalah Rp " + this.limitPinjaman.getAmount());
        }

        //validasi pinjaman aktif
        if (this.hasActiveLoan) {
            throw new IllegalStateException("Lunasi Peminjaman sebelumnya dulu");
        }
    }

    public BorrowerId getId() {
        return id;
    }

    public void setId(BorrowerId id) {
        this.id = id;
    }

    public Money getLimitPinjaman() {
        return limitPinjaman;
    }
    public void setLimitPinjaman(Money limit) {
        this.limitPinjaman = limit;
    }

    public void setKycStatus(boolean status) {
        this.kycStatus = status;
    }
    public boolean isKycStatus() {
        return kycStatus;
    }

    public void setCreditScore(int score) {
        this.creditScore = score;
    }
    public int getCreditScore() {
        return this.creditScore;
    }

    public void setHasActiveLoan(boolean hasActiveLoan) {
        this.hasActiveLoan = hasActiveLoan;
    }
    public boolean hasActiveLoan() {
        return hasActiveLoan;
    }
}