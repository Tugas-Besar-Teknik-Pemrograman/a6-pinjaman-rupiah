package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;
import java.util.Scanner;
import java.util.logging.Logger;

public class MainMenu {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final AppContext ctx;
    private final Scanner scanner;

    public MainMenu(Scanner scanner) {
        this.ctx = AppContext.getInstance();
        this.scanner = scanner;
    }

    public void tampil() {
        logger.info("=== Selamat Datang di P2P Pinjaman Rupiah ===");

        while (true) {
            if (!ctx.isLoggedIn()) {
                boolean keluar = tampilLogin();
                if (keluar) {
                    logger.info("Terima kasih. Sampai jumpa!");
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

    private boolean tampilLogin() {
        logger.info("=== MENU UTAMA ===");
        logger.info("1. Registrasi");
        logger.info("2. Login");
        logger.info("3. Keluar");
        logger.info("Pilih: ");
        String pilihan = scanner.nextLine().trim();

        return switch (pilihan) {
            case "1" -> prosesRegistrasi();
            case "2" -> prosesLogin();
            case "3" -> true;
            default -> {
                logger.warning("Pilihan tidak valid.");
                yield false;
            }
        };
    }

    private boolean prosesRegistrasi() {
        logger.info("REGISTRASI USER");
        logger.info("Nama : ");
        String nama = scanner.nextLine().trim();

        logger.info("Email : ");
        String email = scanner.nextLine().trim();

        logger.info("Password : ");
        String password = scanner.nextLine().trim();

        logger.info("Konfirmasi Password : ");
        String konfirmasiPassword = scanner.nextLine().trim();

        if (!password.equals(konfirmasiPassword)) {
            logger.warning("Registrasi gagal, Password dan konfirmasi password ga cocok");
            return false;
        }

        logger.info("Usia : ");
        int usia;
        try {
            usia = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            logger.warning("Registrasi gagal, Usia harus berupa angka");
            return false;
        }

        logger.info("Pilih Role:");
        logger.info("1. Borrower");
        logger.info("2. Lender");
        logger.info("Pilih Role (1/2): ");
        String rolePilihan = scanner.nextLine().trim();
        int role;
        java.math.BigDecimal penghasilan = java.math.BigDecimal.ZERO;

        if ("1".equals(rolePilihan)) {
            role = 1;
            logger.info("Penghasilan Bulanan (Rp): ");
            try {
                penghasilan = new java.math.BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                logger.warning("Registrasi gagal: Penghasilan harus berupa angka!");
                return false;
            }
        } else if ("2".equals(rolePilihan)) {
            role = 2;
        } else {
            logger.warning("Registrasi gagal: Pilihan role tidak valid!");
            return false;
        }

        try {
            var user = ctx.getUserService().registerUser(nama, email, password, usia, role, penghasilan);
            logger.info("------------------------------------------");
            logger.info("Registrasi Berhasil!");
            logger.log(java.util.logging.Level.INFO, "ID User : {0}", user.getId().getValue());
            logger.log(java.util.logging.Level.INFO, "Role    : {0}", role == 1 ? "BORROWER" : "LENDER");
            if (role == 1) {
                var borrower = ctx.getRepos().getBorrowerRepository().findById(new com.p2p.domain.borrower.BorrowerId(user.getId().getValue()));
                if (borrower != null) {
                    logger.log(java.util.logging.Level.INFO, "Limit Awal (Kapasitas Cicilan Bulanan): Rp {0} (30% x Penghasilan Rp {1})",
                            new Object[]{borrower.getLimitPinjaman().getAmount(), borrower.getPenghasilan().getAmount()});
                    logger.info("Catatan        : Limit pengajuan riil akan dihitung secara dinamis saat pengajuan pinjaman");
                    logger.info("                 tergantung dari tenor dan jenis bunga yang dipilih.");
                }
            }
            logger.info("------------------------------------------");
        } catch (Exception e) {
            logger.warning(() -> "Registrasi gagal: " + e.getMessage());
        }
        return false;
    }

    private boolean prosesLogin() {
        logger.info("=== LOGIN ===");
        int attempt = 0;
        boolean loginSukses = false;

        while (attempt < 3 && !loginSukses) {
            logger.info("Email    : ");
            String email = scanner.nextLine().trim();
            logger.info("Password : ");
            String password = scanner.nextLine().trim();

            if (AppContext.ADMIN_EMAIL.equals(email) && AppContext.ADMIN_PASSWORD.equals(password)) {
                ctx.setCurrentUserId("admin");
                ctx.setCurrentRole("ADMIN");
                logger.info("Login berhasil sebagai Admin.");
                loginSukses = true;
                break;
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
                logger.log(java.util.logging.Level.INFO, "Login berhasil sebagai {0}.", ctx.getCurrentRole());
                loginSukses = true;
            } catch (Exception e) {
                attempt++;
                final int finalAttempt = attempt;
                logger.warning(() -> "Login gagal: " + e.getMessage());
                if (finalAttempt < 3) {
                    logger.warning(() -> "Kesempatan mencoba: " + (3 - finalAttempt) + " kali lagi.");
                } else {
                    logger.warning("Anda telah salah memasukkan email/password sebanyak 3 kali. Kembali ke menu utama.");
                }
            }
        }
        return false;
    }
}
