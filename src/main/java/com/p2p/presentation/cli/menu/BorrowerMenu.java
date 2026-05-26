package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            System.out.println("1. Ajukan Pinjaman Baru");
            System.out.println("2. Lihat Status Pinjaman");
            System.out.println("3. Bayar Cicilan");
            System.out.println("4. Simulasi Jatuh Tempo");
            System.out.println("5. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> menuAjukanPinjaman();
                case "2" -> menuLihatStatusPinjaman();
                case "3" -> menuBayarCicilan();
                case "4" -> menuSimulasiOverdue();
                case "5" -> {
                    ctx.logout();
                    System.out.println("Logout berhasil.");
                    kembali = true;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private void menuAjukanPinjaman() {
        System.out.println("\n=== AJUKAN PINJAMAN BARU ===");
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) return;

        try {
            var borrowerObj = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            System.out.print("Masukkan Nominal Pinjaman (Rp): ");
            long nominal = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Masukkan Tenor (Bulan): ");
            int tenor = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Pilih Jenis Bunga: 1. Syariah 2. Float 3. Flat");
            System.out.print("Pilih (1/2/3): ");
            String pilihanBunga = scanner.nextLine().trim();
            String interestType = switch (pilihanBunga) {
                case "1" -> "syariah";
                case "2" -> "float";
                case "3" -> "flat";
                default -> throw new IllegalArgumentException("Invalid!");
            };

            Money amount = new Money(BigDecimal.valueOf(nominal), "IDR");
            Loan loan = ctx.getLoanService().ajukanPinjaman(new BorrowerId(borrowerIdStr), amount, tenor, interestType);
            System.out.println("Berhasil! ID: " + loan.getId().getValue());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void menuLihatStatusPinjaman() {
        System.out.print("Masukkan Loan ID: ");
        String id = scanner.nextLine().trim();
        try {
            Loan loan = ctx.getLoanService().getLoan(new LoanId(id));
            if (loan == null) {
                throw new Exception("Loan tidak ditemukan.");
            }
            System.out.println("Status: " + loan.getStatus() + " | Sisa: Rp " + loan.getSisaTagihanKeseluruhan().getAmount());
        } catch (Exception e) {
            System.out.println("Gagal: " + e.getMessage());
        }
    }

    private void menuBayarCicilan() {
        System.out.print("Masukkan Loan ID: ");
        String id = scanner.nextLine().trim();
        try {
            Loan loan = ctx.getLoanService().getLoan(new LoanId(id));
            if (loan == null) {
                throw new Exception("Loan tidak ditemukan.");
            }
            System.out.println("Tagihan bulan ini: Rp " + (loan.getTagihanBulanIni() != null ? loan.getTagihanBulanIni().getAmount() : "0"));
            System.out.print("Nominal Bayar: ");
            long bayar = Long.parseLong(scanner.nextLine().trim());
            ctx.getLoanService().bayarCicilan(new LoanId(id), new Money(BigDecimal.valueOf(bayar), "IDR"));
            System.out.println("Pembayaran sukses.");
        } catch (Exception e) {
            System.out.println("Gagal: " + e.getMessage());
        }
    }

    private void menuSimulasiOverdue() {
        System.out.println("1. Majukan Jatuh Tempo | 2. Tandai Overdue");
        String opt = scanner.nextLine();
        System.out.print("Loan ID: ");
        String id = scanner.nextLine();
        try {
            if ("1".equals(opt)) ctx.getLoanService().simulasiMajukanJatuhTempo(new LoanId(id));
            else if ("2".equals(opt)) ctx.getLoanService().tandaiOverdue(new LoanId(id));
            System.out.println("Simulasi sukses.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}