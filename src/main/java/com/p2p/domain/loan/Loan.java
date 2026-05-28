package com.p2p.domain.loan;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.strategy.InterestCalculationStrategy;
import com.p2p.domain.state.LoanStateFactory;
import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Loan {
    private LoanId loanid;
    private BorrowerId borrowerId;
    private Money targetNominal;
    private Money totalTerkumpul;
    private Money sisaPokok;
    private int tenor;
    private int tenorSisa;
    private String status;
    private long maturityDate; // Timestamp jatuh tempo (milliseconds)
    private Money overdueFeesAccrued; // Denda yang terkumpul

    private InterestCalculationStrategy interestStrategy;
    private Money currentMonthBill;
    private Map<LenderId, Money> daftarPendana;

    private LocalDate tanggalDibuat;
    private LocalDate tanggalKadaluarsaFunding;
    private LocalDate tanggalJatuhTempo; 

    private static final int BATAS_HARI_FUNDING = 28;

    public Loan(LoanId loanid, BorrowerId borrowerId, Money targetNominal, int tenor) {
        this.loanid = loanid;
        this.borrowerId = borrowerId;
        this.targetNominal = targetNominal;
        this.tenor = tenor;
        this.tenorSisa = tenor;
        this.sisaPokok = targetNominal;
        this.totalTerkumpul = new Money(BigDecimal.ZERO, "IDR");
        this.daftarPendana = new HashMap<>();
        this.status = "PENDING";
        this.currentMonthBill = new Money(BigDecimal.ZERO, "IDR");
        this.maturityDate = 0; // Belum ada jatuh tempo sampai pencairan disetujui
        this.overdueFeesAccrued = new Money(BigDecimal.ZERO, "IDR");
    }

    public Loan(LoanId loanid, BorrowerId borrowerId, Money targetNominal) {
        this(loanid, borrowerId, targetNominal, 12);
    }

    public void ubahStatus(String statusBaru) {
        this.status = statusBaru;
    }

    public void tambahPendanaan(LenderId lenderId, Money investasiDiberikan) {
        BigDecimal totalBaru = this.totalTerkumpul.getAmount().add(investasiDiberikan.getAmount());

        if (totalBaru.compareTo(this.targetNominal.getAmount()) > 0) {
            throw new IllegalArgumentException("Nominal investasi melebihi target pendanaan");
        }

        this.totalTerkumpul = new Money(totalBaru, this.totalTerkumpul.getCurrency());

        if (this.daftarPendana.containsKey(lenderId)) {
            BigDecimal uangLama = this.daftarPendana.get(lenderId).getAmount();
            BigDecimal akumulasi = uangLama.add(investasiDiberikan.getAmount());
            this.daftarPendana.put(lenderId, new Money(akumulasi, investasiDiberikan.getCurrency()));
        } else {
            this.daftarPendana.put(lenderId, investasiDiberikan);
        }
        
        if (totalBaru.compareTo(this.targetNominal.getAmount()) == 0) {
            LoanStateFactory.fundingReady().ubahStatus(this);
        }
    }

    public void cairkanPinjaman() {
        // Dipanggil saat DISBURSED — cicilan pertama jatuh tempo 28 hari sejak cair
        this.tanggalJatuhTempo = LocalDate.now().plusDays(28);
        LoanStateFactory.disbursed().ubahStatus(this);
        generateMonthlyBill();
    }

    public void bayarCicilan(String repaymentId, Money jumlahBayar) throws Exception {
        bayarCicilan(jumlahBayar);
    }

    public void setInterestStrategy(InterestCalculationStrategy strategy) {
        this.interestStrategy = strategy;
    }

    // MODIFIKASI: Penambahan denda OVERDUE
    public void generateMonthlyBill() {
        if (this.interestStrategy != null) {
            Money tagihanNormal = this.interestStrategy.hitungCicilan(
                    this.targetNominal, this.sisaPokok, this.tenor);
            
            BigDecimal totalAmount = tagihanNormal.getAmount();

            if ("OVERDUE".equals(this.status)) {
                BigDecimal dendaOverdue = new BigDecimal("50000"); // Contoh denda flat 50.000
                totalAmount = totalAmount.add(dendaOverdue);
            }

            this.currentMonthBill = new Money(totalAmount, "IDR");
        }
    }

    public void bayarCicilan(Money paymentAmount) throws Exception {
        if (!"DISBURSED".equals(this.status) && !"REPAYMENT".equals(this.status) && !"OVERDUE".equals(this.status)) {
            throw new Exception("Pinjaman belum dicairkan atau tidak aktif untuk pembayaran.");
        }
        if (this.currentMonthBill == null) {
            throw new Exception("Tidak ada tagihan aktif");
        }
        if (this.currentMonthBill.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Tagihan bulan ini belum tersedia");
        }
        if (paymentAmount.getAmount().compareTo(this.currentMonthBill.getAmount()) < 0) {
            throw new Exception("Nominal pembayaran kurang dari nominal tagihan");
        }

        BigDecimal principalPortion = this.targetNominal.getAmount()
                .divide(new BigDecimal(this.tenor), RoundingMode.HALF_UP);
        this.sisaPokok = new Money(
                this.sisaPokok.getAmount().subtract(principalPortion),
                this.sisaPokok.getCurrency());
                
        this.currentMonthBill = new Money(BigDecimal.ZERO, this.currentMonthBill.getCurrency());
        this.tenorSisa--;

        if (this.tanggalJatuhTempo != null) {
            this.tanggalJatuhTempo = LocalDate.now().plusDays(30);
        }
 
        if (this.status.equals("DISBURSED")) {
            LoanStateFactory.repayment().ubahStatus(this);
        } else if (this.status.equals("OVERDUE")) {
            // Bayar setelah overdue → kembali ke REPAYMENT
            LoanStateFactory.repayment().ubahStatus(this);
        }

        if (isLunas()) {
            LoanStateFactory.closed().ubahStatus(this);
        }
    }

    public Money getSisaTagihanKeseluruhan() {
        if (this.sisaPokok == null) {
            return this.targetNominal;
        }
        return this.sisaPokok;
    }

    public boolean isLunas() {
        return this.tenorSisa <= 0
                || this.sisaPokok.getAmount().compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean isPinjamanExpired() {
        if (!"FUNDING".equals(this.status)) return false;
        return LocalDate.now().isAfter(this.tanggalKadaluarsaFunding);
    }

    public boolean isPinjamanOverdue() {
        if (!"DISBURSED".equals(this.status) && !"REPAYMENT".equals(this.status)) return false;
        if (this.tanggalJatuhTempo == null) return false;
        return LocalDate.now().isAfter(this.tanggalJatuhTempo);
    }

    public boolean apakahOverdueSudahDibayar() {
        if (!"OVERDUE".equals(this.status)) return false;
        if (this.currentMonthBill == null) return false;
        return this.currentMonthBill.getAmount().compareTo(BigDecimal.ZERO) == 0;
    }

    public void setTotalTerkumpul(Money totalTerkumpul) {
        this.totalTerkumpul = totalTerkumpul;
    }

    public Money getTotalTerkumpul() {
        return totalTerkumpul;
    }

    public Money getTargetNominal() {
        return targetNominal;
    }

    public boolean isLayakNotifikasiPencairan() {
        return this.status.equals("DISBURSED");
    }

    public LoanId getId() {
        return loanid;
    }

    public BorrowerId getBorrowerId() {
        return borrowerId;
    }

    public String getStatus() {
        return status;
    }

    public int getTenorSisa() {
        return tenorSisa;
    }

    public Money getTagihanBulanIni() {
        return currentMonthBill;
    }

    public long getTanggalJatuhTempoTimestamp() {
        return maturityDate;
    }

    public void setTanggalJatuhTempoTimestemp(long maturityDate) {
        this.maturityDate = maturityDate;
    }

    public Money getTotalDendaTerkumpul() {
        return overdueFeesAccrued;
    }

    public void setTotalDendaTerkumpul(Money overdueFeesAccrued) {
        this.overdueFeesAccrued = overdueFeesAccrued;
    }

    public void tambahDenda(Money fee) {
        BigDecimal newTotal = this.overdueFeesAccrued.getAmount().add(fee.getAmount());
        this.overdueFeesAccrued = new Money(newTotal, this.overdueFeesAccrued.getCurrency());
    }

    public Map<LenderId, Money> getDaftarPendana() {
        return daftarPendana;
    }

    public Map<LenderId, Money> hitungDistribusiCicilan() {
        Map<LenderId, Money> distribusi = new LinkedHashMap<>();
        if (daftarPendana.isEmpty() || currentMonthBill == null
                || currentMonthBill.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return distribusi;
        }

        BigDecimal totalDana = this.totalTerkumpul.getAmount();
        BigDecimal tagihan   = this.currentMonthBill.getAmount();
        BigDecimal sisa      = tagihan;

        // Urutkan terbesar dulu — sisa rounding jatuh ke lender terkecil (index terakhir)
        List<Map.Entry<LenderId, Money>> sorted = daftarPendana.entrySet().stream()
            .sorted((a, b) -> b.getValue().getAmount().compareTo(a.getValue().getAmount()))
            .toList();

        for (int i = 0; i < sorted.size(); i++) {
            LenderId id = sorted.get(i).getKey();
            BigDecimal bagian;
            if (i == sorted.size() - 1) {
                bagian = sisa;
            } else {
                BigDecimal proporsi = sorted.get(i).getValue().getAmount()
                    .divide(totalDana, 10, RoundingMode.HALF_UP);
                bagian = tagihan.multiply(proporsi).setScale(0, RoundingMode.DOWN);
                sisa = sisa.subtract(bagian);
            }
            distribusi.put(id, new Money(bagian, "IDR"));
        }
        return distribusi;
    }

    public Map<LenderId, Money> getListPendana() {
        return daftarPendana;
    }

    public LocalDate getTanggalJatuhTempo() {
        return tanggalJatuhTempo;
    }

    public void setTanggalJatuhTempo(LocalDate tanggalJatuhTempo) {
        this.tanggalJatuhTempo = tanggalJatuhTempo;
    }

    public void setTanggalKadaluarsaFunding(LocalDate tanggalKadaluarsaFunding) {
        this.tanggalKadaluarsaFunding = tanggalKadaluarsaFunding;
    }

    public Money getSisaPokok() {
        return sisaPokok;
    }
}