package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;
import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
            System.out.println("1. Saldo");
            System.out.println("2. Ajukan Pinjaman Baru");
            System.out.println("3. Lihat Status Pinjaman");
            System.out.println("4. Bayar Cicilan");
            System.out.println("5. Proses Pencairan");
            System.out.println("6. Simulasi Jatuh Tempo");
            System.out.println("7. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> menuSaldo();
                case "2" -> menuAjukanPinjaman();
                case "3" -> menuLihatStatusPinjaman();
                case "4" -> menuBayarCicilan();
                case "5" -> menuProsesPencairan();
                case "6" -> menuSimulasiOverdue();
                case "7" -> {
                    ctx.logout();
                    System.out.println("Logout berhasil.");
                    kembali = true;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private void menuSaldo() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) {
            System.out.println("Gagal, Profil Borrower tidak ditemukan.");
            return;
        }

        try {
            Borrower borrower = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            if (borrower == null) {
                System.out.println("Gagal, data Borrower tidak ditemukan.");
                return;
            }

            System.out.println("\n--- Saldo Borrower ---");
            System.out.println("Saldo saat ini : Rp " + borrower.getSaldoBalance().getAmount());
            System.out.print("Masukkan nominal top up (Rp, 0 untuk batal): ");
            long nominal = Long.parseLong(scanner.nextLine().trim());
            if (nominal <= 0) {
                return;
            }

            borrower.tambahSaldo(new Money(BigDecimal.valueOf(nominal), "IDR"));
            ctx.getRepos().getBorrowerRepository().save(borrower);
            System.out.println("Top up berhasil!");
            System.out.println("Saldo terbaru  : Rp " + borrower.getSaldoBalance().getAmount());
        } catch (NumberFormatException e) {
            System.out.println("Gagal, input nominal harus berupa angka!");
        } catch (Exception e) {
            System.out.println("Gagal top up saldo: " + e.getMessage());
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

    private void menuProsesPencairan() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) {
            System.out.println("Gagal, Profil Borrower tidak ditemukan.");
            return;
        }

        List<Loan> allLoans = ctx.getRepos().getLoanRepository().findAll();
        List<Loan> readyLoans = allLoans.stream()
                .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                .filter(l -> "FUNDING_READY".equals(l.getStatus()))
                .toList();

        if (readyLoans.isEmpty()) {
            System.out.println("Tidak ada pinjaman Anda yang siap untuk pencairan (FUNDING_READY).");
            return;
        }

        Loan selectedLoan;
        if (readyLoans.size() == 1) {
            selectedLoan = readyLoans.get(0);
            System.out.println("Menemukan 1 pinjaman siap dicairkan: " + selectedLoan.getId().getValue() + ". Memproses pencairan...");
        } else {
            System.out.println("\n--- Pinjaman Anda yang Siap Pencairan ---");
            for (int i = 0; i < readyLoans.size(); i++) {
                Loan l = readyLoans.get(i);
                System.out.printf("%d) %s - Target Rp %s - Terkumpul Rp %s%n", i + 1, l.getId().getValue(), l.getTargetNominal().getAmount(), l.getTotalTerkumpul().getAmount());
            }
            System.out.print("Pilih nomor pinjaman untuk dicairkan (atau '0' untuk batal): ");
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) return;
            try {
                int idx = Integer.parseInt(choice) - 1;
                if (idx < 0 || idx >= readyLoans.size()) {
                    System.out.println("Pilihan tidak valid.");
                    return;
                }
                selectedLoan = readyLoans.get(idx);
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid.");
                return;
            }
        }

        try {
            ctx.getLoanService().prosesPencairan(selectedLoan.getId());
            Loan updated = ctx.getRepos().getLoanRepository().findById(selectedLoan.getId());
            System.out.println("Pencairan berhasil diproses! Status: " + updated.getStatus());
        } catch (Exception e) {
            System.out.println("Gagal memproses pencairan: " + e.getMessage());
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