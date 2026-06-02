package com.p2p.domain.loan;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.strategy.InterestCalculationStrategy;
import com.p2p.domain.state.LoanStateFactory;
import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    // bulanKe: tracking cicilan ke berapa yang sedang berjalan
    private int bulanKe;
    private String status;
    private long maturityDate;
    private Money overdueFeesAccrued;

    private InterestCalculationStrategy interestStrategy;
    private String jenisBunga;
    private Money currentMonthBill;
    // dendaBulanIni: denda OVERDUE bulan ini (sisa pokok * 2%), dikirim ke lender
    private Money dendaBulanIni;
    private Map<LenderId, Money> daftarPendana;
    private Money adminFee;

    private LocalDate tanggalKadaluarsaFunding;
    private LocalDate tanggalJatuhTempo;

    private static final int BATAS_HARI_FUNDING = 28;
    private static final BigDecimal RATE_DENDA_OVERDUE = new BigDecimal("0.02");

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_FUNDING = "FUNDING";
    private static final String STATUS_DISBURSED = "DISBURSED";
    private static final String STATUS_REPAYMENT = "REPAYMENT";
    private static final String STATUS_OVERDUE = "OVERDUE";

    public Loan(LoanId loanid, BorrowerId borrowerId, Money targetNominal, int tenor) {
        this.loanid = loanid;
        this.borrowerId = borrowerId;
        this.targetNominal = targetNominal;
        this.tenor = tenor;
        this.tenorSisa = tenor;
        this.bulanKe = 0;
        this.sisaPokok = targetNominal;
        this.totalTerkumpul = new Money(BigDecimal.ZERO, Money.IDR);
        this.daftarPendana = new HashMap<>();
        this.status = STATUS_PENDING;
        this.currentMonthBill = new Money(BigDecimal.ZERO, Money.IDR);
        this.dendaBulanIni = new Money(BigDecimal.ZERO, Money.IDR);
        this.maturityDate = 0;
        this.overdueFeesAccrued = new Money(BigDecimal.ZERO, Money.IDR);
        this.adminFee = new Money(
            targetNominal.getAmount().multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP),
            Money.IDR);
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
            this.daftarPendana.put(lenderId, new Money(uangLama.add(investasiDiberikan.getAmount()), investasiDiberikan.getCurrency()));
        } else {
            this.daftarPendana.put(lenderId, investasiDiberikan);
        }
        if (totalBaru.compareTo(this.targetNominal.getAmount()) == 0) {
            LoanStateFactory.fundingReady().ubahStatus(this);
        }
    }

    public void cairkanPinjaman() {
        this.tanggalJatuhTempo = LocalDate.now().plusDays(BATAS_HARI_FUNDING);
        LoanStateFactory.disbursed().ubahStatus(this);
        generateMonthlyBill();
    }

    public void setInterestStrategy(InterestCalculationStrategy strategy) {
        this.interestStrategy = strategy;
    }

    /**
     * Generate tagihan bulan ini.
     * Jika OVERDUE: Tagihan = Cicilan Normal + Denda (sisaPokok * 2%)
     * Denda disimpan terpisah di dendaBulanIni agar bisa dikirim ke lender.
     */
    public void generateMonthlyBill() {
        if (this.interestStrategy == null) {
            throw new IllegalStateException("Tidak bisa menghitung tagihan: interestStrategy belum di-set pada loan " + this.loanid.getValue());
        }
        Money tagihanNormal = this.interestStrategy.hitungCicilan(
                this.targetNominal, this.sisaPokok, this.tenor);

        if (STATUS_OVERDUE.equals(this.status)) {
            BigDecimal denda = this.sisaPokok.getAmount()
                    .multiply(RATE_DENDA_OVERDUE)
                    .setScale(0, RoundingMode.HALF_UP);
            this.dendaBulanIni = new Money(denda, Money.IDR);
            this.currentMonthBill = new Money(
                    tagihanNormal.getAmount().add(denda), Money.IDR);
        } else {
            this.dendaBulanIni = new Money(BigDecimal.ZERO, Money.IDR);
            this.currentMonthBill = new Money(tagihanNormal.getAmount(), Money.IDR);
        }
    }

    public void bayarCicilan(Money paymentAmount) {
        if (!STATUS_DISBURSED.equals(this.status) && !STATUS_REPAYMENT.equals(this.status) && !STATUS_OVERDUE.equals(this.status)) {
            throw new IllegalStateException("Pinjaman belum dicairkan atau tidak aktif untuk pembayaran.");
        }
        if (this.currentMonthBill == null) {
            throw new IllegalStateException("Tidak ada tagihan aktif");
        }
        if (this.currentMonthBill.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Tagihan bulan ini belum tersedia");
        }
        // Bayar harus pas sesuai tagihan, tidak boleh kurang
        if (paymentAmount.getAmount().compareTo(this.currentMonthBill.getAmount()) < 0) {
            throw new IllegalArgumentException("Nominal pembayaran kurang dari nominal tagihan");
        }

        // Catat denda ke total denda terkumpul sebelum di-nolkan
        if (STATUS_OVERDUE.equals(this.status) && this.dendaBulanIni != null
                && this.dendaBulanIni.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            tambahDenda(this.dendaBulanIni);
        }

        BigDecimal principalPortion = this.targetNominal.getAmount()
                .divide(new BigDecimal(this.tenor), RoundingMode.HALF_UP);
        this.sisaPokok = new Money(
                this.sisaPokok.getAmount().subtract(principalPortion),
                this.sisaPokok.getCurrency());

        this.currentMonthBill = new Money(BigDecimal.ZERO, this.currentMonthBill.getCurrency());
        this.dendaBulanIni = new Money(BigDecimal.ZERO, Money.IDR);
        this.tenorSisa--;
        this.bulanKe++;

        if (this.tanggalJatuhTempo != null) {
            this.tanggalJatuhTempo = LocalDate.now().plusDays(30);
        }

        if (STATUS_DISBURSED.equals(this.status) || STATUS_OVERDUE.equals(this.status)) {
            LoanStateFactory.repayment().ubahStatus(this);
        }

        if (isLunas()) {
            this.sisaPokok = new Money(BigDecimal.ZERO, this.sisaPokok.getCurrency());
            this.currentMonthBill = new Money(BigDecimal.ZERO, this.currentMonthBill.getCurrency());
            this.tenorSisa = 0;
            LoanStateFactory.closed().ubahStatus(this);
        }
    }

    public Money getSisaTagihanKeseluruhan() {
        if (this.sisaPokok == null) return this.targetNominal;
        if (isLunas()) return new Money(BigDecimal.ZERO, this.sisaPokok.getCurrency());
        return this.sisaPokok;
    }

    public boolean isLunas() {
        return this.tenorSisa <= 0
                || this.sisaPokok.getAmount().compareTo(BigDecimal.ZERO) <= 0;
    }

    public boolean isPinjamanExpired() {
        if (!STATUS_FUNDING.equals(this.status)) return false;
        return LocalDate.now().isAfter(this.tanggalKadaluarsaFunding);
    }

    public boolean isPinjamanOverdue() {
        if (!STATUS_DISBURSED.equals(this.status) && !STATUS_REPAYMENT.equals(this.status)) return false;
        if (this.tanggalJatuhTempo == null) return false;
        return LocalDate.now().isAfter(this.tanggalJatuhTempo);
    }

    public boolean apakahOverdueSudahDibayar() {
        if (!STATUS_OVERDUE.equals(this.status)) return false;
        if (this.currentMonthBill == null) return false;
        return this.currentMonthBill.getAmount().compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Distribusi cicilan ke lender.
     * Jika OVERDUE: denda (sisaPokok*2%) ikut masuk ke lender (bukan admin).
     * Distribusi proporsional berdasarkan porsi dana masing-masing lender.
     */
    public Map<LenderId, Money> hitungDistribusiCicilan() {
        Map<LenderId, Money> distribusi = new LinkedHashMap<>();
        if (daftarPendana.isEmpty() || currentMonthBill == null
                || currentMonthBill.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return distribusi;
        }

        BigDecimal totalDana = this.totalTerkumpul.getAmount();
        // PERBAIKAN: semua tagihan (termasuk denda) masuk ke lender
        BigDecimal tagihan = this.currentMonthBill.getAmount();
        BigDecimal sisa = tagihan;

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
            distribusi.put(id, new Money(bagian, Money.IDR));
        }
        return distribusi;
    }

    // ======= Getters & Setters =======

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
        return STATUS_DISBURSED.equals(this.status);
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

    public LoanStatus getStatusEnum() {
        return LoanStatus.fromString(this.status);
    }

    public void setStatusEnum(LoanStatus s) {
        this.status = s == null ? null : s.name();
    }

    public int getTenorSisa() {
        return tenorSisa;
    }

    public int getTenor() {
        return tenor;
    }

    public int getBulanKe() {
        return bulanKe;
    }

    public Money getTagihanBulanIni() {
        return currentMonthBill;
    }

    public Money getDendaBulanIni() {
        return dendaBulanIni;
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

    public Money getAdminFee() {
        return adminFee;
    }

    public String getJenisBunga() {
        return jenisBunga;
    }

    public void setJenisBunga(String jenisBunga) {
        this.jenisBunga = jenisBunga;
    }

    public Money hitungEstimasiCicilan() {
        if (interestStrategy == null) return new Money(BigDecimal.ZERO, Money.IDR);
        return interestStrategy.hitungCicilan(targetNominal, sisaPokok, tenor);
    }

    public Money hitungEstimasiReturnLender(LenderId lenderId) {
        if (!STATUS_DISBURSED.equals(status) && !STATUS_REPAYMENT.equals(status) && !STATUS_OVERDUE.equals(status)) {
            return new Money(BigDecimal.ZERO, Money.IDR);
        }
        Money investasi = daftarPendana.get(lenderId);
        if (investasi == null || targetNominal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new Money(BigDecimal.ZERO, Money.IDR);
        }
        Money cicilan = hitungEstimasiCicilan();
        if (cicilan.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new Money(BigDecimal.ZERO, Money.IDR);
        }
        BigDecimal proporsi = investasi.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(targetNominal.getAmount(), 2, RoundingMode.HALF_UP);
        return cicilan.multiply(proporsi).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }
}