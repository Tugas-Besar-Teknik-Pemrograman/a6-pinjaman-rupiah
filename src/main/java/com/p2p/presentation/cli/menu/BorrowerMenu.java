package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

public class BorrowerMenu {

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
            System.out.println("\n=== MENU BORROWER ===");
            System.out.println("1. Lihat Profil Borrower");
            System.out.println("2. Top Up Saldo");
            System.out.println("3. Ajukan Pinjaman Baru");
            System.out.println("4. Lihat Status Pinjaman");
            System.out.println("5. Bayar Cicilan");
            System.out.println("6. Status Cicilan");
            System.out.println("7. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();
            String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
            if (borrowerIdStr == null) {
                System.out.println("Gagal, Profil Borrower tidak ditemukan.");
                return;
            }
            switch (pilihan) {
                case "1" -> menuProfil();
                case "2" -> menuTopUp();
                case "3" -> menuAjukanPinjaman();
                case "4" -> menuLihatStatusPinjaman();
                case "5" -> menuBayarCicilan();
                case "6" -> menuStatusCicilan();
                case "7" -> {
                    ctx.logout();
                    System.out.println("Logout berhasil.");
                    kembali = true;
                }
                default -> System.out.println("Pilihan tidak valid.");
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

            Loan activeLoan = null;
            if (borrower.hasActiveLoan()) {
                activeLoan = ctx.getRepos().getLoanRepository().findAll().stream()
                        .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                        .filter(l -> !"CLOSED".equals(l.getStatus()) && !"REJECTED".equals(l.getStatus()) && !"CANCELED".equals(l.getStatus()))
                        .findFirst().orElse(null);
            }

            System.out.println("\n=== DASHBOARD BORROWER ===");
            System.out.println("Selamat datang, " + user.getNama() + "!");
            System.out.println();
            System.out.printf("Saldo Anda           : Rp %,.0f%n", borrower.getSaldoBalance().getAmount());
            System.out.printf("Limit Pinjaman       : Rp %,.0f%n", borrower.getLimitPinjaman().getAmount());
            System.out.println("Pinjaman Aktif       : " + (activeLoan != null ? "Ada" : "Tidak Ada"));
            if (activeLoan != null) {
                System.out.println("  - Loan ID          : " + activeLoan.getId().getValue());
                System.out.println("  - Status Pinjaman  : " + activeLoan.getStatus());
                System.out.printf("  - Tagihan Bulan Ini: Rp %,.0f%n",
                        activeLoan.getTagihanBulanIni() != null ? activeLoan.getTagihanBulanIni().getAmount() : BigDecimal.ZERO);
                System.out.println("  - Sisa Tenor       : " + activeLoan.getTenorSisa() + " Bulan");
            }
            System.out.println();
            System.out.print("[Tekan Enter untuk lanjut ke menu] ");
            scanner.nextLine();
        } catch (Exception e) {
            // Dashboard gagal dimuat, lanjut ke menu
        }
    }

    private void menuProfil() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) { System.out.println("Gagal, Profil Borrower tidak ditemukan."); return; }
        try {
            Borrower borrower = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            com.p2p.domain.user.User user = ctx.getRepos().getUserRepository().findById(new com.p2p.domain.user.UserId(ctx.getCurrentUserId()));
            if (borrower == null || user == null) { System.out.println("Gagal, data tidak ditemukan."); return; }

            String activeLoanIdStr = null;
            if (borrower.hasActiveLoan()) {
                activeLoanIdStr = ctx.getRepos().getLoanRepository().findAll().stream()
                        .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                        .filter(l -> !"CLOSED".equals(l.getStatus()) && !"REJECTED".equals(l.getStatus()) && !"CANCELED".equals(l.getStatus()))
                        .map(l -> l.getId().getValue()).findFirst().orElse(null);
            }

            System.out.println("\n=== PROFIL BORROWER ===");
            System.out.println("ID Borrower        : " + borrower.getId().getValue());
            System.out.println("Nama               : " + user.getNama());
            System.out.println("Email              : " + user.getEmail());
            System.out.println("Usia               : " + user.getUsia() + " tahun");
            System.out.println("Penghasilan Bulanan: Rp " + borrower.getPenghasilan().getAmount());
            System.out.println("Limit Pinjaman Awal: Rp " + borrower.getLimitPinjaman().getAmount() + " (30% dari Penghasilan)");
            System.out.println("Status KYC         : " + (borrower.isKycStatus() ? "Terverifikasi" : "Belum Terverifikasi"));
            System.out.println("Credit Score       : " + borrower.getCreditScore());
            System.out.println("Pinjaman Aktif     : " + (borrower.hasActiveLoan() ? "Ada" : "Tidak Ada"));
            if (activeLoanIdStr != null) System.out.println("Loan ID            : " + activeLoanIdStr);
            System.out.println("Saldo Saat Ini     : Rp " + borrower.getSaldoBalance().getAmount());
            System.out.println("=======================");
        } catch (Exception e) {
            System.out.println("Gagal memuat profil: " + e.getMessage());
        }
    }

    private void menuTopUp() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) { System.out.println("Gagal, Profil Borrower tidak ditemukan."); return; }
        try {
            Borrower borrower = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            if (borrower == null) { System.out.println("Gagal, data Borrower tidak ditemukan."); return; }
            System.out.println("\n--- Top Up Saldo Borrower ---");
            System.out.println("Saldo saat ini : Rp " + borrower.getSaldoBalance().getAmount());
            System.out.print("Masukkan nominal top up (Rp, 0 untuk batal): ");
            long nominal = Long.parseLong(scanner.nextLine().trim());
            if (nominal <= 0) return;
            borrower.tambahSaldo(new Money(BigDecimal.valueOf(nominal), "IDR"));
            ctx.getRepos().getBorrowerRepository().save(borrower);
            System.out.println("Top up berhasil! Saldo terbaru: Rp " + borrower.getSaldoBalance().getAmount());
        } catch (NumberFormatException e) {
            System.out.println("Gagal, input nominal harus berupa angka!");
        } catch (Exception e) {
            System.out.println("Gagal top up saldo: " + e.getMessage());
        }
    }

    private void menuAjukanPinjaman() {
        System.out.println("\n=== AJUKAN PINJAMAN BARU ===");
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) return;
        try {
            var borrowerObj = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            if (borrowerObj == null) { System.out.println("Gagal, data Borrower tidak ditemukan."); return; }

            System.out.print("Masukkan Nominal Pinjaman (Rp): ");
            long nominal = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Masukkan Tenor (Bulan): ");
            int tenor = Integer.parseInt(scanner.nextLine().trim());
            System.out.println("Pilih Jenis Bunga: 1. Syariah 2. Float 3. Flat");
            System.out.print("Pilih (1/2/3): ");
            String pilihanBunga = scanner.nextLine().trim();
            String interestType = switch (pilihanBunga) {
                case "1" -> "syariah";
                case "2" -> "float";
                case "3" -> "flat";
                default -> throw new IllegalArgumentException("Pilihan bunga tidak valid!");
            };

            BigDecimal nominalBD = BigDecimal.valueOf(nominal);
            if (!borrowerObj.isKycStatus()) throw new IllegalStateException("Peminjaman ditolak karena Borrower belum terverifikasi (KYC)");
            if (borrowerObj.getCreditScore() < 600) throw new IllegalStateException("Peminjaman ditolak karena Credit score di bawah ambang batas");
            if (nominalBD.compareTo(new BigDecimal("100000")) < 0) throw new IllegalArgumentException("Nominal pinjaman harus lebih dari 100.000");
            if (nominalBD.remainder(new BigDecimal("100000")).compareTo(BigDecimal.ZERO) != 0) throw new IllegalArgumentException("Nominal pinjaman harus kelipatan 100.000");
            if (borrowerObj.hasActiveLoan()) throw new IllegalStateException("Lunasi Peminjaman sebelumnya dulu");

            BigDecimal bungaRate = "syariah".equals(interestType) ? BigDecimal.ZERO : new BigDecimal("0.05");
            Money limitDinamis = borrowerObj.hitungLimitDenganTenorDanBunga(tenor, bungaRate);
            if (limitDinamis.isLessThan(new Money(nominalBD, "IDR"))) {
                throw new IllegalStateException("Pengajuan melebihi limit. Limit Anda: Rp " + String.format("%,.0f", limitDinamis.getAmount()));
            }

            BigDecimal principalPerMonth = nominalBD.divide(BigDecimal.valueOf(tenor), 2, RoundingMode.HALF_UP);
            BigDecimal adminFee = nominalBD.multiply(new BigDecimal("0.01")).setScale(0, RoundingMode.HALF_UP);
            BigDecimal sisaPokok = nominalBD;
            BigDecimal totalInterest = BigDecimal.ZERO;
            BigDecimal totalPengembalian = BigDecimal.ZERO;

            System.out.println("\n=================================================");
            System.out.println("            PREVIEW PENGAJUAN PINJAMAN           ");
            System.out.println("=================================================");
            System.out.printf("Nominal Pinjaman : Rp %,.0f%n", nominalBD);
            System.out.printf("Tenor            : %d Bulan%n", tenor);
            System.out.printf("Jenis Bunga      : %s%n", interestType.toUpperCase());
            System.out.printf("Biaya Admin (1%%) : Rp %,.0f (dipotong saat pencairan)%n", adminFee);
            System.out.println("-------------------------------------------------");
            System.out.println("           Simulasi Cicilan Bulanan              ");
            System.out.println("-------------------------------------------------");

            for (int i = 1; i <= tenor; i++) {
                BigDecimal bunga = BigDecimal.ZERO;
                if ("flat".equals(interestType)) {
                    bunga = nominalBD.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
                } else if ("float".equals(interestType)) {
                    bunga = sisaPokok.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
                } else if ("syariah".equals(interestType)) {
                    bunga = new BigDecimal("150000");
                }
                BigDecimal cicilan = principalPerMonth.add(bunga);
                totalPengembalian = totalPengembalian.add(cicilan);
                totalInterest = totalInterest.add(bunga);
                System.out.printf("Bulan %2d: Pokok Rp %,.0f + %s Rp %,.0f = Cicilan Rp %,.0f%n",
                        i, principalPerMonth, "syariah".equals(interestType) ? "Margin" : "Bunga", bunga, cicilan);
                sisaPokok = sisaPokok.subtract(principalPerMonth);
            }

            System.out.println("-------------------------------------------------");
            System.out.printf("Total Bunga/Margin : Rp %,.0f%n", totalInterest);
            System.out.printf("Total Pengembalian : Rp %,.0f%n", totalPengembalian);
            System.out.println("=================================================");
            System.out.print("Apakah Anda setuju? (y/n): ");
            if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) {
                System.out.println("Pengajuan pinjaman dibatalkan.");
                return;
            }

            Loan loan = ctx.getLoanService().ajukanPinjaman(new BorrowerId(borrowerIdStr), new Money(nominalBD, "IDR"), tenor, interestType);
            System.out.println("Berhasil! ID: " + loan.getId().getValue());
            System.out.printf("Biaya Admin (1%%): Rp %,.0f (dipotong saat pencairan)%n", loan.getAdminFee().getAmount());
            System.out.printf("Estimasi Bersih : Rp %,.0f%n", nominalBD.subtract(loan.getAdminFee().getAmount()));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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
                .filter(l -> !"REJECTED".equals(l.getStatus()) && !"CANCELED".equals(l.getStatus()))
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
            BigDecimal sisaPokok = loan.getTargetNominal().getAmount();
            BigDecimal principalPerMonth = loan.getTargetNominal().getAmount()
                    .divide(BigDecimal.valueOf(totalTenor), 0, RoundingMode.HALF_UP);

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
                        i, statusCicilan, estimasi.getAmount(), sisaPokok);
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
                    .filter(l -> "DISBURSED".equals(l.getStatus()) || "REPAYMENT".equals(l.getStatus()) || "OVERDUE".equals(l.getStatus()))
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
            if (tagihan == null || tagihan.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
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

            if ("OVERDUE".equals(loan.getStatus())) {
                Money denda = loan.getDendaBulanIni();
                BigDecimal cicilanNormal = tagihan.getAmount().subtract(
                        denda != null ? denda.getAmount() : BigDecimal.ZERO);
                System.out.printf("Cicilan Normal    : Rp %,.0f%n", cicilanNormal);
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
}