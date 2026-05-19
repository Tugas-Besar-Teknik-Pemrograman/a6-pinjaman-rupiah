package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;

import java.util.Scanner;

public class BorrowerMenu {

    private final AppContext ctx;
    private final Scanner scanner;

    public BorrowerMenu(Scanner scanner) {
        this.ctx = AppContext.getInstance();
        this.scanner = scanner;
    }

    public void tampil() {
        boolean kembali = false;
        while (!kembali) {
            System.out.println("\n=== MENU BORROWER ===");
            System.out.println("1. Ajukan Pinjaman Baru   [TODO - Faqih]");
            System.out.println("2. Lihat Status Pinjaman  [TODO - Arsel]");
            System.out.println("3. Bayar Cicilan          [TODO - Imam]");
            System.out.println("4. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1", "2", "3" -> System.out.println("Fitur ini belum diimplementasi.");
                case "4" -> {
                    ctx.logout();
                    System.out.println("Logout berhasil.");
                    kembali = true;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }
}
