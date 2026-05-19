package com.p2p.presentation.cli.menu;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.user.User;
import com.p2p.infrastructure.memory.RepositoryFactory;
import com.p2p.presentation.cli.AppContext;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final AppContext ctx;
    private final RepositoryFactory repos;
    private final Scanner scanner;

    public AdminMenu(Scanner scanner) {
        this.ctx = AppContext.getInstance();
        this.repos = ctx.getRepos();
        this.scanner = scanner;
    }

    public void tampil() {
        boolean kembali = false;
        while (!kembali) {
            System.out.println("\n=== MENU ADMIN ===");
            System.out.println("1. Lihat Semua Pengguna");
            System.out.println("2. Approve KYC Borrower");
            System.out.println("3. Approve KYC Lender");
            System.out.println("4. Lihat Semua Pinjaman");
            System.out.println("5. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> lihatSemuaPengguna();
                case "2" -> approveKycBorrower();
                case "3" -> approveKycLender();
                case "4" -> lihatSemuaPinjaman();
                case "5" -> {
                    ctx.logout();
                    System.out.println("Logout berhasil.");
                    kembali = true;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private void lihatSemuaPengguna() {
        List<User> users = repos.getUserRepository().findAll();
        if (users.isEmpty()) {
            System.out.println("Belum ada pengguna terdaftar.");
            return;
        }
        System.out.println("\n--- Daftar Pengguna ---");
        System.out.printf("%-30s %-20s %-25s %-10s%n", "ID", "Nama", "Email", "Role");
        System.out.println("-".repeat(90));
        for (User u : users) {
            System.out.printf("%-30s %-20s %-25s %-10s%n",
                    u.getId(), u.getNama(), u.getEmail(), u.getRole());
        }
    }

    private void approveKycBorrower() {
        System.out.print("Masukkan Borrower ID: ");
        String idInput = scanner.nextLine().trim();
        Borrower borrower = repos.getBorrowerRepository().findById(new BorrowerId(idInput));
        if (borrower == null) {
            System.out.println("Borrower tidak ditemukan.");
            return;
        }
        borrower.setKycStatus(true);
        repos.getBorrowerRepository().save(borrower);
        System.out.println("KYC Borrower " + idInput + " berhasil di-approve.");
    }

    private void approveKycLender() {
        System.out.print("Masukkan Lender ID: ");
        String idInput = scanner.nextLine().trim();
        Lender lender = repos.getLenderRepository().findById(new LenderId(idInput));
        if (lender == null) {
            System.out.println("Lender tidak ditemukan.");
            return;
        }
        lender.setKycStatus(true);
        repos.getLenderRepository().save(lender);
        System.out.println("KYC Lender " + idInput + " berhasil di-approve.");
    }

    private void lihatSemuaPinjaman() {
        List<Loan> loans = repos.getLoanRepository().findAll();
        if (loans.isEmpty()) {
            System.out.println("Belum ada pinjaman.");
            return;
        }
        System.out.println("\n--- Daftar Pinjaman ---");
        System.out.printf("%-30s %-20s %-15s%n", "Loan ID", "Nominal", "Status");
        System.out.println("-".repeat(70));
        for (Loan l : loans) {
            System.out.printf("%-30s %-20s %-15s%n",
                    l.getId(), l.getTargetNominal(), l.getStatus());
        }
    }
}
