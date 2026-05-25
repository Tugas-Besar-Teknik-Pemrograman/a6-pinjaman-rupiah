package com.p2p.presentation.cli.menu;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.valueobject.Money;
import com.p2p.presentation.cli.AppContext;

import java.math.BigDecimal;
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
            System.out.println("2. Lihat Pinjaman yang Bisa Didanai  [TODO - Darva]");
            System.out.println("3. Investasi di Pinjaman             [TODO - Darva]");
            System.out.println("4. Proses Pencairan                  [TODO - Rajbi]");
            System.out.println("5. Tarik Saldo                       [TODO - Darva]");
            System.out.println("6. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> menuTopUp();
                case "2", "3", "4", "5" -> System.out.println("Fitur ini belum diimplementasi.");
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
}
