package com.p2p.presentation.cli.menu;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.ReturnRecord;
import com.p2p.domain.loan.Loan;
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
            System.out.println("1. Lihat Profil");
            System.out.println("2. Tambah Saldo (Top Up)");
            System.out.println("3. Pasar Pinjaman (Lihat & Invest)");
            System.out.println("4. Lihat Portofolio Investasi");
            System.out.println("5. Tarik Saldo");
            System.out.println("6. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> menuProfil();
                case "2" -> menuTopUp();
                case "3" -> menuPasarPinjaman();
                case "4" -> menuPortofolioSaya();
                case "5" -> menuTarikSaldo();
                case "6" -> {
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
                if (!isInvestasiAktif(loan)) continue;

                Money investasiPendana = loan.getDaftarPendana().get(lenderId);
                if (investasiPendana == null) continue;

                BigDecimal investasi = investasiPendana.getAmount();
                totalDiinvestasikan = totalDiinvestasikan.add(investasi);

                if (!isMenghasilkanReturn(loan)) continue;

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

    private void menuPasarPinjaman() {
        System.out.println("\n=== PASAR PINJAMAN ===");

        List<Loan> loanFunding = ctx.getRepos().getLoanRepository().findAll().stream()
                .filter(l -> "FUNDING".equals(l.getStatus()))
                .toList();

        if (loanFunding.isEmpty()) {
            System.out.println("Belum ada pinjaman yang bisa didanai saat ini.");
            return;
        }

        System.out.printf("%-20s %12s %8s %14s %6s %8s %18s%n",
                "Loan ID", "Nominal", "Progress", "Sisa", "Tenor", "Bunga", "Est.Return/Rp1jt");
        System.out.println("-".repeat(90));

        for (Loan loan : loanFunding) {
            BigDecimal target = loan.getTargetNominal().getAmount();
            BigDecimal terkumpul = loan.getTotalTerkumpul().getAmount();
            BigDecimal sisa = target.subtract(terkumpul);
            BigDecimal progres = target.compareTo(BigDecimal.ZERO) > 0
                    ? terkumpul.multiply(BigDecimal.valueOf(100)).divide(target, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal estimasiPerJuta = BigDecimal.ZERO;
            BigDecimal cicilan = loan.hitungEstimasiCicilan().getAmount();
            if (target.compareTo(BigDecimal.ZERO) > 0 && cicilan.compareTo(BigDecimal.ZERO) > 0) {
                estimasiPerJuta = cicilan
                        .multiply(new BigDecimal("1000000"))
                        .divide(target, 0, RoundingMode.HALF_UP);
            }

            String bunga = loan.getJenisBunga() != null ? loan.getJenisBunga().toUpperCase() : "-";

            System.out.printf("%-20s %,12.0f %7.1f%% %,14.0f %5d %8s %,18.0f%n",
                    loan.getId().getValue(), target, progres, sisa,
                    loan.getTenor(), bunga, estimasiPerJuta);
        }
        System.out.println("-".repeat(90));

        System.out.print("\nMasukkan Loan ID untuk melihat detail (atau Enter untuk batal): ");
        String loanIdStr = scanner.nextLine().trim();
        if (loanIdStr.isEmpty()) return;

        Loan dipilih = loanFunding.stream()
                .filter(l -> l.getId().getValue().equals(loanIdStr))
                .findFirst().orElse(null);

        if (dipilih == null) {
            System.out.println("Loan ID tidak ditemukan.");
            return;
        }

        tampilDetailLoan(dipilih);

        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        LenderId lenderId = new LenderId(lenderIdStr);
        Lender lender = ctx.getRepos().getLenderRepository().findById(lenderId);

        System.out.println("\nSaldo Anda saat ini: Rp " + String.format("%,.0f", lender.getSaldoBalance().getAmount()));
        System.out.print("Masukkan nominal investasi (Rp, min 100.000, kelipatan 100.000, 0 untuk batal): ");
        try {
            long nominal = Long.parseLong(scanner.nextLine().trim());
            if (nominal == 0) {
                System.out.println("Investasi dibatalkan.");
                return;
            }

            BigDecimal target = dipilih.getTargetNominal().getAmount();
            BigDecimal sisa = target.subtract(dipilih.getTotalTerkumpul().getAmount());
            if (BigDecimal.valueOf(nominal).compareTo(lender.getSaldoBalance().getAmount()) > 0) {
                System.out.println("Gagal investasi: Saldo tidak cukup.");
                return;
            }
            if (BigDecimal.valueOf(nominal).compareTo(sisa) > 0) {
                System.out.println("Gagal investasi: Nominal melebihi sisa kebutuhan pendanaan (Rp " +
                        String.format("%,.0f", sisa) + ").");
                return;
            }

            ctx.getFundingService().invest(lenderId, dipilih.getId(), new Money(BigDecimal.valueOf(nominal), "IDR"));

            Lender updated = ctx.getRepos().getLenderRepository().findById(lenderId);
            System.out.println("Investasi berhasil!");
            System.out.println("Saldo terbaru: Rp " + String.format("%,.0f", updated.getSaldoBalance().getAmount()));
        } catch (NumberFormatException e) {
            System.out.println("Gagal, input nominal harus berupa angka!");
        } catch (Exception e) {
            System.out.println("Gagal investasi: " + e.getMessage());
        }
    }

    private void tampilDetailLoan(Loan loan) {
        BigDecimal target = loan.getTargetNominal().getAmount();
        BigDecimal terkumpul = loan.getTotalTerkumpul().getAmount();
        BigDecimal sisa = target.subtract(terkumpul);
        BigDecimal progres = target.compareTo(BigDecimal.ZERO) > 0
                ? terkumpul.multiply(BigDecimal.valueOf(100)).divide(target, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal estimasiPerJuta = BigDecimal.ZERO;
        BigDecimal cicilan = loan.hitungEstimasiCicilan().getAmount();
        if (target.compareTo(BigDecimal.ZERO) > 0 && cicilan.compareTo(BigDecimal.ZERO) > 0) {
            estimasiPerJuta = cicilan
                    .multiply(new BigDecimal("1000000"))
                    .divide(target, 0, RoundingMode.HALF_UP);
        }

        int creditScore = 0;
        try {
            var borrower = ctx.getRepos().getBorrowerRepository()
                    .findById(new com.p2p.domain.borrower.BorrowerId(loan.getBorrowerId().getValue()));
            if (borrower != null) creditScore = borrower.getCreditScore();
        } catch (Exception ignored) {}

        String bunga = loan.getJenisBunga() != null ? loan.getJenisBunga().toUpperCase() : "-";

        System.out.println("\n+--------------------------------------------------+");
        System.out.printf("| %-48s |%n", "DETAIL PINJAMAN");
        System.out.println("+--------------------------------------------------+");
        System.out.printf("| %-20s: %-27s |%n", "Loan ID", loan.getId().getValue());
        System.out.printf("| %-20s: Rp %-24s |%n", "Nominal", String.format("%,.0f", target));
        System.out.printf("| %-20s: %-27s |%n", "Tenor", loan.getTenor() + " bulan");
        System.out.printf("| %-20s: %-27s |%n", "Jenis Bunga", bunga);
        System.out.printf("| %-20s: %-27s |%n", "Credit Score (anonim)", creditScore > 0 ? String.valueOf(creditScore) : "-");
        System.out.printf("| %-20s: Rp %-18s (%s%%) |%n", "Terkumpul",
                String.format("%,.0f", terkumpul), progres);
        System.out.printf("| %-20s: Rp %-24s |%n", "Sisa Dibutuhkan", String.format("%,.0f", sisa));
        System.out.printf("| %-20s: Rp %-24s |%n", "Est. Return/Rp 1 jt", String.format("%,.0f", estimasiPerJuta));
        System.out.println("+--------------------------------------------------+");
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

    private void menuPortofolioSaya() {
        boolean kembali = false;
        while (!kembali) {
            System.out.println("\n=== PORTOFOLIO SAYA ===");
            System.out.println("1. Investasi Aktif");
            System.out.println("2. Riwayat Return Diterima");
            System.out.println("0. Kembali");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();
            switch (pilihan) {
                case "1" -> menuInvestasiAktif();
                case "2" -> menuRiwayatReturn();
                case "0" -> kembali = true;
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    /** Investasi masih aktif selama loan belum berakhir (lunas/ditolak/dibatalkan). */
    private boolean isInvestasiAktif(Loan loan) {
        String s = loan.getStatus();
        return !"CLOSED".equals(s) && !"REJECTED".equals(s) && !"CANCELED".equals(s);
    }

    /** Loan menghasilkan cicilan bulanan hanya setelah dicairkan dan sedang berjalan. */
    private boolean isMenghasilkanReturn(Loan loan) {
        String s = loan.getStatus();
        return "DISBURSED".equals(s) || "REPAYMENT".equals(s) || "OVERDUE".equals(s);
    }

    private void menuInvestasiAktif() {
        System.out.println("\n--- Investasi Aktif ---");

        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        if (lenderIdStr == null) {
            System.out.println("Gagal, Profil Lender tidak ditemukan.");
            return;
        }

        try {
            LenderId lenderId = new LenderId(lenderIdStr);
            List<Loan> aktif = ctx.getRepos().getLoanRepository().findByLenderId(lenderId).stream()
                    .filter(this::isInvestasiAktif)
                    .toList();

            if (aktif.isEmpty()) {
                System.out.println("Anda belum memiliki investasi aktif.");
                return;
            }

            System.out.printf("%-20s %-14s %14s %8s %18s%n",
                    "Loan ID", "Status", "Investasi", "Proporsi", "Est.Return/Bulan");
            System.out.println("-".repeat(78));

            for (Loan loan : aktif) {
                Money investasiPendana = loan.getDaftarPendana().get(lenderId);
                if (investasiPendana == null) continue;

                BigDecimal investasi = investasiPendana.getAmount();
                BigDecimal target = loan.getTargetNominal().getAmount();
                if (target.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal proporsi = investasi
                        .multiply(BigDecimal.valueOf(100))
                        .divide(target, 2, RoundingMode.HALF_UP);

                BigDecimal bagianReturn = BigDecimal.ZERO;
                if (isMenghasilkanReturn(loan)) {
                    BigDecimal cicilan = loan.hitungEstimasiCicilan().getAmount();
                    bagianReturn = cicilan.multiply(proporsi)
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                }

                System.out.printf("%-20s %-14s %,14.0f %7.1f%% %,18.0f%n",
                        loan.getId().getValue(), loan.getStatus(),
                        investasi, proporsi, bagianReturn);
            }
            System.out.println("-".repeat(78));
        } catch (Exception e) {
            System.out.println("Gagal memuat portofolio: " + e.getMessage());
        }
    }

    private void menuRiwayatReturn() {
        System.out.println("\n--- Riwayat Return Diterima ---");

        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        if (lenderIdStr == null) {
            System.out.println("Gagal, Profil Lender tidak ditemukan.");
            return;
        }

        try {
            LenderId lenderId = new LenderId(lenderIdStr);
            Lender lender = ctx.getRepos().getLenderRepository().findById(lenderId);
            if (lender == null) {
                System.out.println("Data lender tidak ditemukan.");
                return;
            }

            List<ReturnRecord> riwayat = lender.getRiwayatReturn();
            if (riwayat.isEmpty()) {
                System.out.println("Belum ada return yang diterima.");
                return;
            }

            System.out.printf("%-12s %-20s %16s%n", "Tanggal", "Loan ID", "Return Diterima");
            System.out.println("-".repeat(52));

            BigDecimal totalReturn = BigDecimal.ZERO;
            for (ReturnRecord r : riwayat) {
                System.out.printf("%-12s %-20s %,16.0f%n",
                        r.getTanggal(), r.getLoanId().getValue(), r.getJumlah().getAmount());
                totalReturn = totalReturn.add(r.getJumlah().getAmount());
            }
            System.out.println("-".repeat(52));
            System.out.printf("%-34s %,16.0f%n", "Total Return", totalReturn);
        } catch (Exception e) {
            System.out.println("Gagal memuat riwayat return: " + e.getMessage());
        }
    }
}
