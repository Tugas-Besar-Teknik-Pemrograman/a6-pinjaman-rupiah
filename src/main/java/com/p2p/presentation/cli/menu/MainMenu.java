package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;
import java.util.Scanner;

public class MainMenu {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_BORROWER = "BORROWER";
    public static final String ROLE_LENDER = "LENDER";

    private final AppContext ctx;
    private final Scanner scanner;

    public MainMenu(Scanner scanner) {
        this.ctx = AppContext.getInstance();
        this.scanner = scanner;
    }

    public void tampil() {
        System.out.println("=== Selamat Datang di P2P Pinjaman Rupiah ===");

        while (true) {
            if (!ctx.isLoggedIn()) {
                boolean keluar = tampilLogin();
                if (keluar) {
                    System.out.println("Terima kasih. Sampai jumpa!");
                    return;
                }
            } else {
                String role = ctx.getCurrentRole();
                if (ROLE_ADMIN.equals(role)) {
                    new AdminMenu(scanner).tampil();
                } else if (ROLE_BORROWER.equals(role)) {
                    new BorrowerMenu(scanner).tampil();
                } else if (ROLE_LENDER.equals(role)) {
                    new LenderMenu(scanner).tampil();
                }
            }
        }
    }

    private boolean tampilLogin() {
        System.out.println();
        System.out.println("=== MENU UTAMA ===");
        System.out.println("1. Registrasi");
        System.out.println("2. Login");
        System.out.println("3. Keluar");
        System.out.print("Pilih: ");
        String pilihan = scanner.nextLine().trim();

        return switch (pilihan) {
            case "1" -> prosesRegistrasi();
            case "2" -> prosesLogin();
            case "3" -> true;
            default -> {
                System.out.println("Pilihan tidak valid.");
                yield false;
            }
        };
    }

    private boolean prosesRegistrasi() {
        System.out.println("REGISTRASI USER");
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
            System.out.println("Role    : " + (role == 1 ? ROLE_BORROWER : ROLE_LENDER));
            if (role == 1) {
                var borrower = ctx.getRepos().getBorrowerRepository().findById(new com.p2p.domain.borrower.BorrowerId(user.getId().getValue()));
                if (borrower != null) {
                    System.out.printf("Limit Awal (Kapasitas Cicilan Bulanan): Rp %s (30%% x Penghasilan Rp %s)%n",
                            borrower.getLimitPinjaman().getAmount(), borrower.getPenghasilan().getAmount());
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

    private boolean prosesLogin() {
        System.out.println("=== LOGIN ===");
        int attempt = 0;
        boolean loginSukses = false;

        while (attempt < 3 && !loginSukses) {
            System.out.print("Email    : ");
            String email = scanner.nextLine().trim();
            System.out.print("Password : ");
            String password = scanner.nextLine().trim();

            try {
                if (checkAdminLogin(email, password)) {
                    loginSukses = true;
                    break;
                }

                var user = ctx.getUserService().login(email, password);
                String userIdStr = user.getId().getValue();
                ctx.setCurrentUserId(userIdStr);

                setRoleUntukUser(userIdStr);

                System.out.println("Login berhasil sebagai " + ctx.getCurrentRole() + ".");
                loginSukses = true;
            } catch (Exception e) {
                attempt++;
                tanganiLoginGagal(attempt, e.getMessage());
            }
        }
        return false;
    }

    private boolean checkAdminLogin(String email, String password) {
        if (AppContext.ADMIN_EMAIL.equalsIgnoreCase(email)) {
            if (AppContext.ADMIN_PASSWORD.equals(password)) {
                ctx.setCurrentUserId("admin");
                ctx.setCurrentRole(ROLE_ADMIN);
                System.out.println("Login berhasil sebagai Admin.");
                return true;
            } else {
                throw new IllegalArgumentException("Password salah");
            }
        }
        return false;
    }

    private void setRoleUntukUser(String userIdStr) {
        var borrower = ctx.getRepos().getBorrowerRepository().findById(new com.p2p.domain.borrower.BorrowerId(userIdStr));
        if (borrower != null) {
            ctx.linkBorrower(userIdStr, userIdStr);
            ctx.setCurrentRole(ROLE_BORROWER);
            return;
        }
        var lender = ctx.getRepos().getLenderRepository().findById(new com.p2p.domain.lender.LenderId(userIdStr));
        if (lender != null) {
            ctx.linkLender(userIdStr, userIdStr);
            ctx.setCurrentRole(ROLE_LENDER);
            return;
        }
        ctx.setCurrentRole("USER");
    }

    private void tanganiLoginGagal(int attempt, String errMsg) {
        System.out.println("Login gagal: " + errMsg);
        if (attempt < 3) {
            System.out.println("Kesempatan mencoba: " + (3 - attempt) + " kali lagi.");
        } else {
            System.out.println("Anda telah salah memasukkan email/password sebanyak 3 kali. Kembali ke menu utama.");
        }
    }
}
