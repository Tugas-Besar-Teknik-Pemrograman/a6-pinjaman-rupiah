package com.p2p.presentation.cli;

import com.p2p.presentation.cli.menu.AdminMenu;
import com.p2p.presentation.cli.menu.BorrowerMenu;
import com.p2p.presentation.cli.menu.LenderMenu;

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
                    new BorrowerMenu(scanner).tampil();
                } else if ("LENDER".equals(role)) {
                    new LenderMenu(scanner).tampil();
                }
            }
        }
    }

    private static boolean tampilLogin(AppContext ctx, Scanner scanner) {
        System.out.println("\n=== MENU UTAMA ===");
        System.out.println("1. Registrasi");
        System.out.println("2. Login");
        System.out.println("3. Keluar");
        System.out.print("Pilih: ");
        String pilihan = scanner.nextLine().trim();

        switch (pilihan) {
            case "1" -> {
                System.out.println("\nREGISTRASI USER");
                System.out.print("Nama : ");
                String nama = scanner.nextLine().trim();

                System.out.print("Email : ");
                String email = scanner.nextLine().trim();

                System.out.print("Password : ");
                String password = scanner.nextLine().trim();

                System.out.print("Konfirmasi Password : ");
                String konfirmasiPassword = scanner.nextLine().trim();

                if (!password.equals(konfirmasiPassword)) {
                    System.out.println("Registrasi gagal, Password dan konfirmasi password ga cocok");
                    return false;
                }

                System.out.print("Usia : ");
                int usia;
                try {
                    usia = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Registrasi gagal, Usia harus berupa angka");
                    return false;
                }

                System.out.println("Pilih Role:");
                System.out.println("1. Borrower");
                System.out.println("2. Lender");
                System.out.print("Pilih Role (1/2): ");
                String rolePilihan = scanner.nextLine().trim();
                int role;
                java.math.BigDecimal penghasilan = java.math.BigDecimal.ZERO;

                if ("1".equals(rolePilihan)) {
                    role = 1;
                    System.out.print("Penghasilan Bulanan (Rp): ");
                    try {
                        penghasilan = new java.math.BigDecimal(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Registrasi gagal: Penghasilan harus berupa angka!");
                        return false;
                    }
                } else if ("2".equals(rolePilihan)) {
                    role = 2;
                } else {
                    System.out.println("Registrasi gagal: Pilihan role tidak valid!");
                    return false;
                }

                try {
                    var user = ctx.getUserService().registerUser(nama, email, password, usia, role, penghasilan);
                    System.out.println("------------------------------------------");
                    System.out.println("Registrasi Berhasil!");
                    System.out.println("ID User : " + user.getId().getValue());
                    System.out.println("Role    : " + (role == 1 ? "BORROWER" : "LENDER"));
                    if (role == 1) {
                        var borrower = ctx.getRepos().getBorrowerRepository().findById(new com.p2p.domain.borrower.BorrowerId(user.getId().getValue()));
                        if (borrower != null) {
                            System.out.println("Limit Awal (Kapasitas Cicilan Bulanan): Rp " + borrower.getLimitPinjaman().getAmount() + " (30% x Penghasilan Rp " + borrower.getPenghasilan().getAmount() + ")");
                            System.out.println("Catatan        : Limit pengajuan riil akan dihitung secara dinamis saat pengajuan pinjaman");
                            System.out.println("                 tergantung dari tenor dan jenis bunga yang dipilih.");
                        }
                    }
                    System.out.println("------------------------------------------");
                } catch (Exception e) {
                    System.out.println("Registrasi gagal: " + e.getMessage());
                }
                return false;
            }
            case "2" -> {
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
                    String userIdStr = user.getId().getValue();
                    ctx.setCurrentUserId(userIdStr);

                    // Dynamic role resolution by checking repositories
                    var borrower = ctx.getRepos().getBorrowerRepository().findById(new com.p2p.domain.borrower.BorrowerId(userIdStr));
                    if (borrower != null) {
                        ctx.linkBorrower(userIdStr, userIdStr);
                        ctx.setCurrentRole("BORROWER");
                    } else {
                        var lender = ctx.getRepos().getLenderRepository().findById(new com.p2p.domain.lender.LenderId(userIdStr));
                        if (lender != null) {
                            ctx.linkLender(userIdStr, userIdStr);
                            ctx.setCurrentRole("LENDER");
                        } else {
                            ctx.setCurrentRole("USER");
                        }
                    }
                    System.out.println("Login berhasil sebagai " + ctx.getCurrentRole() + ".");
                } catch (Exception e) {
                    System.out.println("Login gagal: " + e.getMessage());
                }
                return false;
            }
            case "3" -> {
                return true;
            }
            default -> {
                System.out.println("Pilihan tidak valid.");
                return false;
            }
        }
    }
}
