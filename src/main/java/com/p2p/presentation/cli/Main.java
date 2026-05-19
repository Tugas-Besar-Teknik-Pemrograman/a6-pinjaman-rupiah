package com.p2p.presentation.cli;

import com.p2p.presentation.cli.menu.AdminMenu;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        AppContext ctx = AppContext.getInstance();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Selamat Datang di P2P Pinjaman Rupiah ===");

        while (true) {
            if (!ctx.isLoggedIn()) {
                boolean keluar = tampilLogin(ctx, scanner);
                if (keluar) {
                    System.out.println("Terima kasih. Sampai jumpa!");
                    scanner.close();
                    return;
                }
            } else {
                String role = ctx.getCurrentRole();
                if ("ADMIN".equals(role)) {
                    new AdminMenu(scanner).tampil();
                } else if ("BORROWER".equals(role)) {
                    System.out.println("[Menu Borrower belum tersedia - coming soon]");
                    ctx.logout();
                } else if ("LENDER".equals(role)) {
                    System.out.println("[Menu Lender belum tersedia - coming soon]");
                    ctx.logout();
                }
            }
        }
    }

    private static boolean tampilLogin(AppContext ctx, Scanner scanner) {
        System.out.println("\n=== MENU UTAMA ===");
        System.out.println("1. Login");
        System.out.println("2. Keluar");
        System.out.print("Pilih: ");
        String pilihan = scanner.nextLine().trim();

        if ("2".equals(pilihan)) {
            return true;
        }

        System.out.println("\n=== LOGIN ===");
        System.out.print("Email    : ");
        String email = scanner.nextLine().trim();
        System.out.print("Password : ");
        String password = scanner.nextLine().trim();

        if (AppContext.ADMIN_EMAIL.equals(email) && AppContext.ADMIN_PASSWORD.equals(password)) {
            ctx.setCurrentUserId("admin");
            ctx.setCurrentRole("ADMIN");
            System.out.println("Login berhasil sebagai Admin.");
            return false;
        }

        try {
            var user = ctx.getUserService().login(email, password);
            ctx.setCurrentUserId(user.getId().toString());

            String borrowerId = ctx.getBorrowerId(user.getId().toString());
            String lenderId   = ctx.getLenderId(user.getId().toString());

            if (borrowerId != null) {
                ctx.setCurrentRole("BORROWER");
            } else if (lenderId != null) {
                ctx.setCurrentRole("LENDER");
            } else {
                ctx.setCurrentRole("USER");
            }
            System.out.println("Login berhasil sebagai " + ctx.getCurrentRole() + ".");
        } catch (Exception e) {
            System.out.println("Login gagal: " + e.getMessage());
        }
        return false;
    }
}
