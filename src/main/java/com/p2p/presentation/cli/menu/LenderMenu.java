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
        boolean kembali = false;
        while (!kembali) {
            System.out.println("\n=== MENU LENDER ===");
            System.out.println("1. Tambah Saldo (Top Up)");
            System.out.println("2. Lihat Pinjaman yang Bisa Didanai");
            System.out.println("3. Investasi di Pinjaman");
            System.out.println("4. Proses Pencairan");
            System.out.println("5. Tarik Saldo");
            System.out.println("6. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> menuTopUp();
                case "2" -> menuLihatPinjamanFunding();
                case "3" -> menuInvestasi();
                case "4" -> prosesPencairan();
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
            BigDecimal progres = terkumpul
                    .multiply(BigDecimal.valueOf(100))
                    .divide(target, 2, RoundingMode.HALF_UP);

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
        System.out.print("Masukkan nominal investasi (Rp): ");
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

    private void prosesPencairan() {
        String lenderIdStr = ctx.getLenderId(ctx.getCurrentUserId());
        if (lenderIdStr == null) {
            System.out.println("Gagal, Profil Lender tidak ditemukan.");
            return;
        }
        LenderId lenderId = new LenderId(lenderIdStr);

        List<Loan> allLoans = ctx.getRepos().getLoanRepository().findAll();
        List<Loan> lenderLoans = allLoans.stream()
                .filter(l -> l.getDaftarPendana().containsKey(lenderId))
                .toList();

        if (lenderLoans.isEmpty()) {
            System.out.println("\nAnda belum menginvestasikan dana pada pinjaman manapun.");
            return;
        }

        System.out.println("\n--- Daftar Pinjaman yang Anda Danai ---");
        System.out.printf("%-3s %-15s %-15s %-15s %-15s %-15s%n", "No", "Loan ID", "Target Nominal", "Dana Anda", "Terkumpul", "Status");
        System.out.println("-".repeat(95));
        for (int i = 0; i < lenderLoans.size(); i++) {
            Loan l = lenderLoans.get(i);
            Money danaLender = l.getDaftarPendana().get(lenderId);
            System.out.printf("%-3d %-15s Rp %-12s Rp %-12s Rp %-12s %-15s%n",
                    i + 1,
                    l.getId().getValue(),
                    l.getTargetNominal().getAmount().toString(),
                    danaLender.getAmount().toString(),
                    l.getTotalTerkumpul().getAmount().toString(),
                    l.getStatus());
        }

        System.out.print("\nPilih no pinjaman untuk memproses pencairan (atau '0' untuk batal): ");
        String choice = scanner.nextLine().trim();
        if ("0".equals(choice)) {
            return;
        }

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index < 0 || index >= lenderLoans.size()) {
                System.out.println("Pilihan tidak valid.");
                return;
            }
            Loan selectedLoan = lenderLoans.get(index);

            System.out.println("\nMemproses pencairan untuk Loan " + selectedLoan.getId().getValue() + "...");
            try {
                ctx.getLoanService().prosesPencairan(selectedLoan.getId());
                Loan updatedLoan = ctx.getRepos().getLoanRepository().findById(selectedLoan.getId());
                System.out.println("Pencairan berhasil diproses!");
                System.out.println("Status pinjaman saat ini: " + updatedLoan.getStatus());
            } catch (Exception e) {
                System.out.println("Gagal memproses pencairan: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("Input tidak valid. Harap masukkan nomor.");
        }
    }
}
