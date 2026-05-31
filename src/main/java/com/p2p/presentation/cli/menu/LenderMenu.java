package com.p2p.presentation.cli.menu;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.valueobject.Money;
import com.p2p.presentation.cli.AppContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

public class LenderMenu {

    private final AppContext ctx;
    private final Scanner scanner;

    public LenderMenu(Scanner scanner) {
        this.ctx = AppContext.getInstance();
        this.scanner = scanner;
    }

    public void tampil() {
        tampilDashboard();
        boolean kembali = false;
        while (!kembali) {
            System.out.println("\n=== MENU LENDER ===");
            System.out.println("1. Lihat Profil Lender");
            System.out.println("2. Tambah Saldo (Top Up)");
            System.out.println("3. Lihat Pinjaman yang Bisa Didanai");
            System.out.println("4. Investasi di Pinjaman");
            System.out.println("5. Lihat Portofolio Investasi");
            System.out.println("6. Tarik Saldo");
            System.out.println("7. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> menuProfil();
                case "2" -> menuTopUp();
                case "3" -> menuLihatPinjamanFunding();
                case "4" -> menuInvestasi();
                case "5" -> menuPortofolioInvestasi();
                case "6" -> menuTarikSaldo();
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
        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        if (lenderIdStr == null) return;

        try {
            LenderId lenderId = new LenderId(lenderIdStr);
            Lender lender = ctx.getRepos().getLenderRepository().findById(lenderId);
            com.p2p.domain.user.User user = ctx.getRepos().getUserRepository()
                    .findById(new com.p2p.domain.user.UserId(ctx.getCurrentUserId()));
            if (lender == null || user == null) return;

            List<Loan> portofolio = ctx.getRepos().getLoanRepository().findByLenderId(lenderId);
            BigDecimal totalDiinvestasikan = BigDecimal.ZERO;
            BigDecimal estimasiReturnPerBulan = BigDecimal.ZERO;

            for (Loan loan : portofolio) {
                Money investasiPendana = loan.getDaftarPendana().get(lenderId);
                if (investasiPendana == null) continue;

                BigDecimal investasi = investasiPendana.getAmount();
                totalDiinvestasikan = totalDiinvestasikan.add(investasi);

                BigDecimal target = loan.getTargetNominal().getAmount();
                if (target.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal cicilanEstimasi = loan.hitungEstimasiCicilan().getAmount();
                    if (cicilanEstimasi.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal proporsi = investasi
                                .multiply(BigDecimal.valueOf(100))
                                .divide(target, 2, RoundingMode.HALF_UP);
                        BigDecimal bagianReturn = cicilanEstimasi.multiply(proporsi)
                                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                        estimasiReturnPerBulan = estimasiReturnPerBulan.add(bagianReturn);
                    }
                }
            }

            System.out.println("\n=== DASHBOARD LENDER ===");
            System.out.println("Selamat datang, " + user.getNama() + "!");
            System.out.println();
            System.out.printf("Saldo Anda           : Rp %,.0f%n", lender.getSaldoBalance().getAmount());
            System.out.printf("Total Diinvestasikan : Rp %,.0f%n", totalDiinvestasikan);
            System.out.printf("Estimasi Return/Bulan: Rp %,.0f%n", estimasiReturnPerBulan);
            System.out.println();
            System.out.print("[Tekan Enter untuk lanjut ke menu] ");
            scanner.nextLine();
        } catch (Exception e) {
            // Dashboard gagal dimuat, lanjut ke menu
        }
    }

    private void menuProfil() {
        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        if (lenderIdStr == null) {
            System.out.println("Gagal, Profil Lender tidak ditemukan.");
            return;
        }

        try {
            Lender lender = ctx.getRepos().getLenderRepository().findById(new LenderId(lenderIdStr));
            com.p2p.domain.user.User user = ctx.getRepos().getUserRepository()
                    .findById(new com.p2p.domain.user.UserId(ctx.getCurrentUserId()));
            if (lender == null || user == null) {
                System.out.println("Gagal, data Lender/User tidak ditemukan.");
                return;
            }

            System.out.println("\n=== PROFIL LENDER ===");
            System.out.println("ID Lender          : " + lender.getId().getValue());
            System.out.println("Nama               : " + user.getNama());
            System.out.println("Email              : " + user.getEmail());
            System.out.println("Usia               : " + user.getUsia() + " tahun");
            System.out.println(
                    "Status KYC         : " + (lender.isKycVerified() ? "Terverifikasi" : "Belum Terverifikasi"));
            System.out.println("Saldo Saat Ini     : Rp " + lender.getSaldoBalance().getAmount());
            System.out.println("=======================");
        } catch (Exception e) {
            System.out.println("Gagal memuat profil: " + e.getMessage());
        }
    }

    private void menuTopUp() {
        System.out.println("\n--- Tambah Saldo (Top Up) ---");

        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        LenderId lenderId = new LenderId(lenderIdStr);
        Lender lender = ctx.getRepos().getLenderRepository().findById(lenderId);
        if (lender == null) {
            System.out.println("Gagal, data Lender tidak ditemukan.");
            return;
        }

        System.out.println("Saldo saat ini : Rp " + lender.getSaldoBalance().getAmount());
        System.out.print("Masukkan nominal top up (Rp): ");
        try {
            long nominal = Long.parseLong(scanner.nextLine().trim());

            Money nominalTambah = new Money(BigDecimal.valueOf(nominal), "IDR");
            lender.tambahSaldo(nominalTambah);
            ctx.getRepos().getLenderRepository().save(lender);

            System.out.println("Top up berhasil!");
            System.out.println("Saldo terbaru  : Rp " + lender.getSaldoBalance().getAmount());
        } catch (NumberFormatException e) {
            System.out.println("Gagal, input nominal harus berupa angka!");
        } catch (Exception e) {
            System.out.println("Gagal top up: " + e.getMessage());
        }
    }

    private void menuLihatPinjamanFunding() {
        System.out.println("\n--- Daftar Pinjaman yang Bisa Didanai ---");
        List<Loan> semuaLoan = ctx.getRepos().getLoanRepository().findAll();
        List<Loan> loanFunding = semuaLoan.stream()
                .filter(l -> "FUNDING".equals(l.getStatus()))
                .toList();

        if (loanFunding.isEmpty()) {
            System.out.println("Belum ada pinjaman yang bisa didanai saat ini.");
            return;
        }

        for (Loan loan : loanFunding) {
            BigDecimal target = loan.getTargetNominal().getAmount();
            BigDecimal terkumpul = loan.getTotalTerkumpul().getAmount();
            BigDecimal sisa = target.subtract(terkumpul);
            BigDecimal progres = target.compareTo(BigDecimal.ZERO) > 0
                    ? terkumpul.multiply(BigDecimal.valueOf(100)).divide(target, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            System.out.println("------------------------------------------");
            System.out.println("ID Pinjaman      : " + loan.getId().getValue());
            System.out.println("Target Nominal   : Rp " + target);
            System.out.println("Total Terkumpul  : Rp " + terkumpul + " (" + progres + "%)");
            System.out.println("Sisa Dibutuhkan  : Rp " + sisa);
        }
        System.out.println("------------------------------------------");
    }

    private void menuInvestasi() {
        System.out.println("\n--- Investasi di Pinjaman ---");
        System.out.print("Masukkan Loan ID: ");
        String loanIdStr = scanner.nextLine().trim();
        System.out.print("Masukkan nominal investasi (Rp, minimal 100.000, kelipatan 100.000): ");
        try {
            long nominal = Long.parseLong(scanner.nextLine().trim());

            String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
            LenderId lenderId = new LenderId(lenderIdStr);
            LoanId loanId = new LoanId(loanIdStr);
            Money amount = new Money(BigDecimal.valueOf(nominal), "IDR");

            ctx.getFundingService().invest(lenderId, loanId, amount);

            Lender lender = ctx.getRepos().getLenderRepository().findById(lenderId);
            System.out.println("Investasi berhasil!");
            System.out.println("Saldo terbaru : Rp " + lender.getSaldoBalance().getAmount());
        } catch (NumberFormatException e) {
            System.out.println("Gagal, input nominal harus berupa angka!");
        } catch (Exception e) {
            System.out.println("Gagal investasi: " + e.getMessage());
        }
    }

    private void menuTarikSaldo() {
        System.out.println("\n--- Tarik Saldo ---");

        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        LenderId lenderId = new LenderId(lenderIdStr);
        Lender lender = ctx.getRepos().getLenderRepository().findById(lenderId);
        if (lender == null) {
            System.out.println("Gagal, data Lender tidak ditemukan.");
            return;
        }

        System.out.println("Saldo saat ini : Rp " + lender.getSaldoBalance().getAmount());
        System.out.print("Masukkan nominal penarikan (Rp, minimal 100000): ");
        try {
            long nominal = Long.parseLong(scanner.nextLine().trim());
            Money amount = new Money(BigDecimal.valueOf(nominal), "IDR");

            ctx.getWithdrawalService().withdraw(lenderId, amount);

            Lender updated = ctx.getRepos().getLenderRepository().findById(lenderId);
            System.out.println("Penarikan berhasil!");
            System.out.println("Saldo terbaru  : Rp " + updated.getSaldoBalance().getAmount());
        } catch (NumberFormatException e) {
            System.out.println("Gagal, input nominal harus berupa angka!");
        } catch (Exception e) {
            System.out.println("Gagal tarik saldo: " + e.getMessage());
        }
    }

    private void menuPortofolioInvestasi() {
        System.out.println("\n--- Portofolio Investasi Saya ---");

        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        if (lenderIdStr == null) {
            System.out.println("Gagal, Profil Lender tidak ditemukan.");
            return;
        }

        try {
            LenderId lenderId = new LenderId(lenderIdStr);

            List<Loan> portofolio = ctx.getRepos().getLoanRepository().findByLenderId(lenderId);

            if (portofolio.isEmpty()) {
                System.out.println("Anda belum memiliki investasi aktif.");
                return;
            }

            for (Loan loan : portofolio) {
                Money investasiPendana = loan.getDaftarPendana().get(lenderId);
                if (investasiPendana == null) {
                    continue;
                }
                BigDecimal investasi = investasiPendana.getAmount();
                BigDecimal target = loan.getTargetNominal().getAmount();
                if (target.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal proporsi = investasi
                        .multiply(BigDecimal.valueOf(100))
                        .divide(target, 2, RoundingMode.HALF_UP);

                System.out.println("------------------------------------------");
                System.out.println("ID Pinjaman   : " + loan.getId().getValue());
                System.out.println("Status        : " + loan.getStatus());
                System.out.println("Investasi     : Rp " + investasi + " (" + proporsi + "%)");

                BigDecimal tagihan = loan.getTagihanBulanIni() != null
                        ? loan.getTagihanBulanIni().getAmount()
                        : BigDecimal.ZERO;
                if (tagihan.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal bagianTagihan = tagihan.multiply(proporsi).divide(BigDecimal.valueOf(100), 0,
                            RoundingMode.HALF_UP);
                    System.out.println("Tagihan Bulan : Rp " + bagianTagihan + " (estimasi)");
                }
            }
            System.out.println("------------------------------------------");
        } catch (Exception e) {
            System.out.println("Gagal memuat portofolio: " + e.getMessage());
        }
    }

    private void prosesPencairan() {
        System.out.println("\nFitur 'Proses Pencairan' dinonaktifkan pada menu Lender.");
        System.out.println(
                "Pencairan kini dikelola melalui menu Borrower. Mohon minta borrower untuk melakukan pencairan.");
    }
}
