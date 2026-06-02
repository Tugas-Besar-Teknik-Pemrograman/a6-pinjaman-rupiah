package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanStatus;
import com.p2p.domain.valueobject.Money;
import com.p2p.domain.loan.strategy.FixedInterestStrategy;
import com.p2p.domain.loan.strategy.FloatingInterestStrategy;
import com.p2p.domain.loan.strategy.SyariahInterestStrategy;
import com.p2p.domain.loan.strategy.InterestCalculationStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class BorrowerMenu {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final AppContext ctx;
    private final Scanner scanner;

    public BorrowerMenu(Scanner scanner) {
        this.ctx = AppContext.getInstance();
        this.scanner = scanner;
    }

    public void tampil() {
        tampilDashboard();
        boolean kembali = false;
        while (!kembali) {
            logger.info("=== MENU BORROWER ===");
            logger.info("1. Lihat Profil Borrower");
            logger.info("2. Top Up Saldo");
            logger.info("3. Ajukan Pinjaman Baru");
            logger.info("4. Lihat Status Pinjaman");
            logger.info("5. Bayar Cicilan");
            logger.info("6. Kotak Notifikasi");
            logger.info("7. Logout");
            logger.info("Pilih: ");
            String pilihan = scanner.nextLine().trim();
            String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
            if (borrowerIdStr == null) {
                logger.warning("Gagal, Profil Borrower tidak ditemukan.");
                return;
            }
            switch (pilihan) {
                case "1" -> menuProfil();
                case "2" -> menuTopUp();
                case "3" -> menuAjukanPinjaman();
                case "4" -> menuLihatStatusPinjaman();
                case "5" -> menuBayarCicilan();
                case "6" -> menuKotakNotifikasi();
                case "7" -> {
                    ctx.logout();
                    logger.info("Logout berhasil.");
                    kembali = true;
                }
                default -> logger.warning("Pilihan tidak valid.");
            }
        }
    }

    private void tampilDashboard() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) return;
        try {
            Borrower borrower = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            com.p2p.domain.user.User user = ctx.getRepos().getUserRepository()
                    .findById(new com.p2p.domain.user.UserId(ctx.getCurrentUserId()));
            if (borrower == null || user == null) return;

            final Loan activeLoan = !borrower.hasActiveLoan() ? null : ctx.getRepos().getLoanRepository().findAll().stream()
                    .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                    .filter(l -> {
                        LoanStatus s = l.getStatusEnum();
                        return s != LoanStatus.CLOSED && s != LoanStatus.REJECTED && s != LoanStatus.CANCELED;
                    })
                    .findFirst().orElse(null);

            logger.info("=== DASHBOARD BORROWER ===");
            logger.info(() -> "Selamat datang, " + user.getNama() + "!");
            logger.info(() -> String.format("Saldo Anda           : Rp %,.0f", borrower.getSaldoBalance().getAmount()));
            logger.info(() -> String.format("Limit Pinjaman       : Rp %,.0f", borrower.getLimitPinjaman().getAmount()));
            logger.info(() -> "Pinjaman Aktif       : " + (activeLoan != null ? "Ada" : "Tidak Ada"));
            if (activeLoan != null) {
                logger.info(() -> "  - Loan ID          : " + activeLoan.getId().getValue());
                logger.info(() -> "  - Status Pinjaman  : " + activeLoan.getStatus());
                logger.info(() -> String.format("  - Tagihan Bulan Ini: Rp %,.0f",
                        activeLoan.getTagihanBulanIni() != null ? activeLoan.getTagihanBulanIni().getAmount() : BigDecimal.ZERO));
                logger.info(() -> "  - Sisa Tenor       : " + activeLoan.getTenorSisa() + " Bulan");
            }
            logger.info("[Tekan Enter untuk lanjut ke menu] ");
            scanner.nextLine();
        } catch (Exception e) {
            logger.warning(() -> "Gagal memuat dashboard: " + e.getMessage());
            // Dashboard gagal dimuat, lanjut ke menu
        }
    }

    private void menuProfil() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) { logger.warning("Gagal, Profil Borrower tidak ditemukan."); return; }
        try {
            Borrower borrower = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            com.p2p.domain.user.User user = ctx.getRepos().getUserRepository().findById(new com.p2p.domain.user.UserId(ctx.getCurrentUserId()));
            if (borrower == null || user == null) { logger.warning("Gagal, data tidak ditemukan."); return; }

            final String activeLoanIdStr = !borrower.hasActiveLoan() ? null : ctx.getRepos().getLoanRepository().findAll().stream()
                    .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                    .filter(l -> {
                        LoanStatus s = l.getStatusEnum();
                        return s != LoanStatus.CLOSED && s != LoanStatus.REJECTED && s != LoanStatus.CANCELED;
                    })
                    .map(l -> l.getId().getValue()).findFirst().orElse(null);

            logger.info("=== PROFIL BORROWER ===");
            logger.info(() -> "ID Borrower        : " + borrower.getId().getValue());
            logger.info(() -> "Nama               : " + user.getNama());
            logger.info(() -> "Email              : " + user.getEmail());
            logger.info(() -> "Usia               : " + user.getUsia() + " tahun");
            logger.info(() -> "Penghasilan Bulanan: Rp " + borrower.getPenghasilan().getAmount());
            logger.info(() -> "Limit Pinjaman Awal: Rp " + borrower.getLimitPinjaman().getAmount() + " (30% dari Penghasilan)");
            logger.info(() -> "Status KYC         : " + (borrower.isKycStatus() ? "Terverifikasi" : "Belum Terverifikasi"));
            logger.info(() -> "Credit Score       : " + borrower.getCreditScore());
            logger.info(() -> "Pinjaman Aktif     : " + (borrower.hasActiveLoan() ? "Ada" : "Tidak Ada"));
            if (activeLoanIdStr != null) {
                logger.info(() -> "Loan ID            : " + activeLoanIdStr);
            }
            logger.info(() -> "Saldo Saat Ini     : Rp " + borrower.getSaldoBalance().getAmount());
            logger.info("=======================");
        } catch (Exception e) {
            logger.warning(() -> "Gagal memuat profil: " + e.getMessage());
        }
    }

    private void menuTopUp() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) { logger.warning("Gagal, Profil Borrower tidak ditemukan."); return; }
        try {
            Borrower borrower = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            if (borrower == null) { logger.warning("Gagal, data Borrower tidak ditemukan."); return; }
            logger.info("--- Top Up Saldo Borrower ---");
            logger.info(() -> "Saldo saat ini : Rp " + borrower.getSaldoBalance().getAmount());
            logger.info("Masukkan nominal top up (Rp, 0 untuk batal): ");
            long nominal = Long.parseLong(scanner.nextLine().trim());
            if (nominal <= 0) return;
            borrower.tambahSaldo(new Money(BigDecimal.valueOf(nominal), Money.IDR));
            ctx.getRepos().getBorrowerRepository().save(borrower);
            logger.info(() -> "Top up berhasil! Saldo terbaru: Rp " + borrower.getSaldoBalance().getAmount());
        } catch (NumberFormatException e) {
            logger.warning("Gagal, input nominal harus berupa angka!");
        } catch (Exception e) {
            logger.warning(() -> "Gagal top up saldo: " + e.getMessage());
        }
    }

    private void menuAjukanPinjaman() {
        logger.info("=== AJUKAN PINJAMAN BARU ===");
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) return;
        try {
            var borrowerObj = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            if (borrowerObj == null) { logger.warning("Gagal, data Borrower tidak ditemukan."); return; }

            logger.info("Masukkan Nominal Pinjaman (Rp): ");
            long nominal = Long.parseLong(scanner.nextLine().trim());
            logger.info("Masukkan Tenor (Bulan): ");
            int tenor = Integer.parseInt(scanner.nextLine().trim());
            logger.info("Pilih Jenis Bunga: 1. Syariah 2. Float 3. Flat");
            logger.info("Pilih (1/2/3): ");
            String pilihanBunga = scanner.nextLine().trim();
            String interestType = switch (pilihanBunga) {
                case "1" -> "syariah";
                case "2" -> "float";
                case "3" -> "flat";
                default -> throw new IllegalArgumentException("Pilihan bunga tidak valid!");
            };

            Money nominalPinjaman = new Money(BigDecimal.valueOf(nominal), Money.IDR);
            // B2: Validasi (KYC, credit score, minimal, kelipatan, active loan, limit) diserahkan
            // sepenuhnya ke Borrower.validasiPinjaman via ajukanPinjaman — tidak diduplikasi di sini.

            Money principalPerMonth = nominalPinjaman.divide(BigDecimal.valueOf(tenor), RoundingMode.HALF_UP);
            Money adminFee = nominalPinjaman.multiply(new BigDecimal("0.01"));
            Money sisaPokok = nominalPinjaman;
            Money totalInterest = new Money(BigDecimal.ZERO, Money.IDR);
            Money totalPengembalian = new Money(BigDecimal.ZERO, Money.IDR);

            logger.info("=================================================");
            logger.info("            PREVIEW PENGAJUAN PINJAMAN           ");
            logger.info("=================================================");
            logger.info(() -> String.format("Nominal Pinjaman : Rp %,.0f", nominalPinjaman.getAmount()));
            logger.info(() -> String.format("Tenor            : %d Bulan", tenor));
            logger.info(() -> String.format("Jenis Bunga      : %s", interestType.toUpperCase()));
            logger.info(() -> String.format("Biaya Admin (1%%) : Rp %,.0f (dipotong saat pencairan)", adminFee.getAmount()));
            logger.info("-------------------------------------------------");
            logger.info("           Simulasi Cicilan Bulanan              ");
            logger.info("-------------------------------------------------");

            // B1: Gunakan strategy (sumber kebenaran dari domain) — bukan duplikasi rumus
            InterestCalculationStrategy strategy;
            if ("flat".equals(interestType)) {
                strategy = new FixedInterestStrategy(new BigDecimal("0.05"));
            } else if ("float".equals(interestType)) {
                strategy = new FloatingInterestStrategy(new BigDecimal("0.05"));
            } else { // syariah
                strategy = new SyariahInterestStrategy(new BigDecimal("150000"));
            }
            for (int i = 1; i <= tenor; i++) {
                final int index = i;
                Money cicilan = strategy.hitungCicilan(nominalPinjaman, sisaPokok, tenor);
                Money bungaBagian = cicilan.subtract(principalPerMonth);
                totalPengembalian = totalPengembalian.add(cicilan);
                totalInterest = totalInterest.add(bungaBagian);
                logger.info(() -> String.format("Bulan %2d: Pokok Rp %,.0f + %s Rp %,.0f = Cicilan Rp %,.0f",
                        index, principalPerMonth.getAmount(), "syariah".equals(interestType) ? "Margin" : "Bunga",
                        bungaBagian.getAmount(), cicilan.getAmount()));
                sisaPokok = sisaPokok.subtract(principalPerMonth);
            }

            logger.info("-------------------------------------------------");
            final Money finalTotalInterest = totalInterest;
            final Money finalTotalPengembalian = totalPengembalian;
            logger.info(() -> String.format("Total Bunga/Margin : Rp %,.0f", finalTotalInterest.getAmount()));
            logger.info(() -> String.format("Total Pengembalian : Rp %,.0f", finalTotalPengembalian.getAmount()));
            logger.info("=================================================");
            logger.info("Apakah Anda setuju? (y/n): ");
            if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) {
                logger.info("Pengajuan pinjaman dibatalkan.");
                return;
            }

            Loan loan = ctx.getLoanService().ajukanPinjaman(new BorrowerId(borrowerIdStr), nominalPinjaman, tenor, interestType);
            logger.info(() -> "Berhasil! ID: " + loan.getId().getValue());
            logger.info(() -> String.format("Biaya Admin (1%%): Rp %,.0f (dipotong saat pencairan)", loan.getAdminFee().getAmount()));
            logger.info(() -> String.format("Estimasi Bersih : Rp %,.0f", nominalPinjaman.subtract(loan.getAdminFee()).getAmount()));
        } catch (Exception e) {
            logger.warning(() -> "Error: " + e.getMessage());
        }
    }

    private void menuLihatStatusPinjaman() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) { System.out.println("Gagal, Profil Borrower tidak ditemukan."); return; }

        List<Loan> borrowerLoans = ctx.getRepos().getLoanRepository().findAll().stream()
                .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                .toList();

        System.out.println("\n--- Status Pinjaman Anda ---");
        if (borrowerLoans.isEmpty()) { System.out.println("Anda belum memiliki pinjaman."); return; }

        for (Loan loan : borrowerLoans) {
            System.out.printf("Loan ID : %s%n", loan.getId().getValue());
            System.out.printf("Status  : %s%n", loan.getStatus());
            System.out.printf("Nominal : Rp %,.0f%n", loan.getTargetNominal().getAmount());
            System.out.printf("Sisa Pokok : Rp %,.0f%n", loan.getSisaTagihanKeseluruhan().getAmount());
            System.out.printf("Tenor   : %d bulan tersisa dari %d%n", loan.getTenorSisa(), loan.getTenor());
            if (loan.getTagihanBulanIni() != null && loan.getTagihanBulanIni().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                System.out.printf("Tagihan Bulan Ini : Rp %,.0f%n", loan.getTagihanBulanIni().getAmount());
            }
            System.out.println("------------------------------");
        }
    }

    /**
     * POIN 2: Menu status cicilan — tampilkan cicilan yang sudah dan belum dibayar
     */
    private void menuStatusCicilan() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) { System.out.println("Gagal, Profil Borrower tidak ditemukan."); return; }

        List<Loan> activeLoans = ctx.getRepos().getLoanRepository().findAll().stream()
                .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                .filter(l -> {
                    LoanStatus s = l.getStatusEnum();
                    return s != LoanStatus.REJECTED && s != LoanStatus.CANCELED;
                })
                .toList();

        if (activeLoans.isEmpty()) { System.out.println("Anda belum memiliki pinjaman."); return; }

        for (Loan loan : activeLoans) {
            int totalTenor = loan.getTenor();
            int sudahDibayar = totalTenor - loan.getTenorSisa();
            int belumDibayar = loan.getTenorSisa();

            System.out.println("\n=== STATUS CICILAN ===");
            System.out.println("Loan ID    : " + loan.getId().getValue());
            System.out.println("Status     : " + loan.getStatus());
            System.out.printf("Nominal    : Rp %,.0f%n", loan.getTargetNominal().getAmount());
            System.out.printf("Sisa Pokok : Rp %,.0f%n", loan.getSisaTagihanKeseluruhan().getAmount());
            System.out.println("------------------------------");
            System.out.printf("Total Tenor    : %d bulan%n", totalTenor);
            System.out.printf("Sudah Dibayar  : %d cicilan (bulan 1 s/d %d)%n", sudahDibayar, sudahDibayar);
            System.out.printf("Belum Dibayar  : %d cicilan (bulan %d s/d %d)%n", belumDibayar, sudahDibayar + 1, totalTenor);
            System.out.println("------------------------------");

            // Detail per cicilan
            Money sisaPokok = loan.getTargetNominal();
            Money principalPerMonth = loan.getTargetNominal().divide(BigDecimal.valueOf(totalTenor), RoundingMode.HALF_UP);

            for (int i = 1; i <= totalTenor; i++) {
                String statusCicilan;
                if (i <= sudahDibayar) {
                    statusCicilan = "[✓] LUNAS";
                } else if (i == sudahDibayar + 1) {
                    statusCicilan = "[→] TAGIHAN SAAT INI";
                } else {
                    statusCicilan = "[ ] BELUM";
                }
                // Estimasi cicilan (tanpa denda, hanya pokok+bunga normal)
                Money estimasi = loan.hitungEstimasiCicilan();
                System.out.printf("Cicilan ke-%2d: %s  | Est. Rp %,.0f | Sisa Pokok: Rp %,.0f%n",
                        i, statusCicilan, estimasi.getAmount(), sisaPokok.getAmount());
                sisaPokok = sisaPokok.subtract(principalPerMonth);
            }
            System.out.println("==============================");
        }
    }

    /**
     * POIN 3 & 4: Bayar cicilan harus pas sesuai tagihan + tampilkan cicilan ke berapa
     */
    private void menuBayarCicilan() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) { System.out.println("Gagal, Profil Borrower tidak ditemukan."); return; }

        try {
            List<Loan> activeLoans = ctx.getRepos().getLoanRepository().findAll().stream()
                    .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                    .filter(l -> {
                        LoanStatus s = l.getStatusEnum();
                        return s == LoanStatus.DISBURSED || s == LoanStatus.REPAYMENT || s == LoanStatus.OVERDUE;
                    })
                    .toList();

            if (activeLoans.isEmpty()) {
                System.out.println("Anda tidak memiliki pinjaman aktif yang perlu dibayar.");
                return;
            }

            Loan loan = activeLoans.get(0);
            LoanId id = loan.getId();
            int totalTenor = loan.getTenor();
            int sudahDibayar = totalTenor - loan.getTenorSisa();
            int bulanSekarang = sudahDibayar + 1;

            Money tagihan = loan.getTagihanBulanIni();
            if (tagihan == null || !tagihan.isGreaterThan(new Money(BigDecimal.ZERO, tagihan.getCurrency()))) {
                System.out.println("Tagihan bulan ini belum tersedia. Silakan simulasi tenor berikutnya terlebih dahulu.");
                return;
            }

            System.out.println("\n=== PEMBAYARAN CICILAN ===");
            System.out.println("Loan ID          : " + id.getValue());
            // POIN 4: tampilkan cicilan ke berapa
            System.out.printf("Cicilan ke        : %d dari %d%n", bulanSekarang, totalTenor);
            System.out.printf("Sisa Tenor        : %d bulan lagi%n", loan.getTenorSisa());
            System.out.printf("Sisa Pokok        : Rp %,.0f%n", loan.getSisaTagihanKeseluruhan().getAmount());
            System.out.println("Status Pinjaman  : " + loan.getStatus());

            if (loan.getStatusEnum() == LoanStatus.OVERDUE) {
                Money denda = loan.getDendaBulanIni();
                Money cicilanNormal = tagihan.subtract(denda != null ? denda : new Money(BigDecimal.ZERO, tagihan.getCurrency()));
                System.out.printf("Cicilan Normal    : Rp %,.0f%n", cicilanNormal.getAmount());
                System.out.printf("Denda Overdue (2%%): Rp %,.0f%n", denda != null ? denda.getAmount() : BigDecimal.ZERO);
            }

            System.out.printf("Total Tagihan     : Rp %,.0f%n", tagihan.getAmount());
            System.out.printf("Saldo Anda        : Rp %,.0f%n",
                    ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr)).getSaldoBalance().getAmount());
            System.out.println("--------------------------");
            // POIN 3: bayar harus pas, langsung pakai tagihan
            System.out.println("Pembayaran akan dilakukan sebesar tagihan penuh.");
            System.out.print("Konfirmasi bayar Rp " + String.format("%,.0f", tagihan.getAmount()) + "? (y/n): ");
            if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) {
                System.out.println("Pembayaran dibatalkan.");
                return;
            }

            ctx.getLoanService().bayarCicilan(id, tagihan);

            System.out.println("\n✓ Pembayaran cicilan ke-" + bulanSekarang + " berhasil!");
            System.out.printf("  Dibayar  : Rp %,.0f%n", tagihan.getAmount());
            if (loan.isLunas()) {
                System.out.println("  Status   : LUNAS - Selamat, pinjaman Anda telah selesai!");
            } else {
                System.out.printf("  Sisa     : %d cicilan lagi%n", loan.getTenorSisa());
            }
        } catch (Exception e) {
            System.out.println("Gagal: " + e.getMessage());
        }
    }

    private void menuKotakNotifikasi() {
    String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
    if (borrowerIdStr == null) {
        System.out.println("Gagal, Profil Borrower tidak ditemukan.");
        return;
    }

    System.out.println("\n=== KOTAK NOTIFIKASI ===");
    List<String> notifs = ctx.getNotificationService().getNotifikasi(borrowerIdStr);
    if (notifs.isEmpty()) {
        System.out.println("Belum ada notifikasi.");
    } else {
        System.out.println("Notifikasi terbaru:");
        for (int i = 0; i < notifs.size(); i++) {
            System.out.println("  [" + (i + 1) + "] " + notifs.get(i));
        }
    }
    System.out.println("========================");
}
}