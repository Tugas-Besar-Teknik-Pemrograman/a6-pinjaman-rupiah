package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;

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
        System.out.println("Fitur ini belum diimplementasi.");
    }
}
