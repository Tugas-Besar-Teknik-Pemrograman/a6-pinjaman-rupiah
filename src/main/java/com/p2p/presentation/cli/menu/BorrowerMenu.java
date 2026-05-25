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
            System.out.println("2. Lihat Status Pinjaman  [TODO - Arsel]");
            System.out.println("3. Bayar Cicilan          [DONE - Imam]");
            System.out.println("4. Simulasi Jatuh Tempo   [DONE - Imam]");
            System.out.println("5. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
               
                case "1" -> {
                    System.out.println("\nAJUKAN PINJAMAN BARU");
                    String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
                    if (borrowerIdStr == null) {
                        System.out.println("Gagal, Profil Borrower tidak ditemukan.");
                        break;
                    }

                    System.out.print("Masukkan Nominal Pinjaman (Rp): ");
                    try {
                        long nominal = Long.parseLong(scanner.nextLine().trim());
                        System.out.print("Masukkan Tenor (Bulan): ");
                        int tenor = Integer.parseInt(scanner.nextLine().trim());

                        Money amount = new Money(BigDecimal.valueOf(nominal), "IDR");
                        Loan loan = ctx.getLoanService().ajukanPinjaman(new BorrowerId(borrowerIdStr), amount, tenor);

                        System.out.println("\nPengajuan pinjaman berhasil diajukan!");
                        System.out.println("   Loan ID : " + loan.getId().getValue());
                        System.out.println("   Status  : " + loan.getStatus());

                    } catch (NumberFormatException e) {
                        System.out.println("\nGagal, Input nominal/tenor harus berupa angka!");
                    } catch (Exception e) {
                        System.out.println("\nGagal mengajukan pinjaman: " + e.getMessage());
                    }
                }

                case "2" -> {
                    System.out.println("\n=== STATUS PINJAMAN ===");
                    System.out.print("Masukkan ID Pinjaman (Loan ID): ");
                    String inputLoanId = scanner.nextLine().trim();

                    try {
                        Loan loan = ctx.getLoanService().getLoan(new LoanId(inputLoanId));
                        BigDecimal totalTerkumpul = loan.getTotalTerkumpul().getAmount();
                        BigDecimal targetNominal = loan.getTargetNominal().getAmount();
                        BigDecimal progresPendanaan = BigDecimal.ZERO;
                        if (targetNominal.compareTo(BigDecimal.ZERO) > 0) {
                            progresPendanaan = totalTerkumpul
                                    .multiply(new BigDecimal("100"))
                                    .divide(targetNominal, 2, RoundingMode.HALF_UP);
                        }

                        System.out.println("------------------------------------------");
                        System.out.println("ID Pinjaman      : " + loan.getId().getValue());
                        System.out.println("Status Pinjaman  : " + loan.getStatus());
                        System.out.println("Target Nominal   : Rp " + targetNominal);
                        System.out.println("Total Terkumpul  : Rp " + totalTerkumpul + " (" + progresPendanaan + "%)");
                        System.out.println("Sisa Tenor       : " + loan.getTenorSisa() + " bulan");
                        System.out.println("Sisa Tagihan     : Rp " + loan.getSisaTagihanKeseluruhan().getAmount());
                        
                        if (loan.getCurrentMonthBill() != null && loan.getCurrentMonthBill().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                            System.out.println("Tagihan Bulan Ini: Rp " + loan.getCurrentMonthBill().getAmount());
                        } else {
                            System.out.println("Tagihan Bulan Ini: -");
                        }
                        System.out.println("Jatuh Tempo Berikutnya: " + (loan.getTanggalJatuhTempo() != null ? loan.getTanggalJatuhTempo() : "-"));
                        System.out.println("\nRiwayat Pendana:");
                        if (loan.getListPendana().isEmpty()) {
                            System.out.println("- Belum ada pendana.");
                        } else {
                            loan.getListPendana().forEach((lenderId, amount) ->
                                    System.out.println("- " + lenderId + " : Rp " + amount.getAmount()));
                        }
                        System.out.println("------------------------------------------");
                    } catch (Exception e) {
                        System.out.println("Gagal mengambil status pinjaman: " + e.getMessage());
                    }
                }

                case "3" -> {
                    menuBayarCicilan();
                }

                case "4" -> {
                    menuSimulasiOverdue();
                }
                
                case "5" -> {
                    ctx.logout();
                    System.out.println("Logout berhasil.");
                    kembali = true;
                }

                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    // Submenu: Bayar Cicilan
    private void menuBayarCicilan() {
        System.out.println("\n=== BAYAR CICILAN ===");
        System.out.print("Masukkan ID Pinjaman (Loan ID): ");
        String inputLoanId = scanner.nextLine().trim();

        // Tampilkan tagihan aktif sebelum minta nominal pembayaran
        try {
            Loan loan = ctx.getLoanService().getLoan(new LoanId(inputLoanId));
            if (loan == null) {
                System.out.println("Error: Pinjaman dengan ID tersebut tidak ditemukan.");
                return;
            }

            String status = loan.getStatus();
            if (!"DISBURSED".equals(status) && !"REPAYMENT".equals(status) && !"OVERDUE".equals(status)) {
                System.out.println("Pinjaman tidak dalam status pembayaran (status saat ini: " + status + ").");
                return;
            }

            // Tampilkan ringkasan tagihan
            System.out.println("   Status Pinjaman   : " + status);
            System.out.println("   Sisa Pokok        : Rp " + loan.getSisaTagihanKeseluruhan().getAmount());
            System.out.println("   Sisa Tenor        : " + loan.getTenorSisa() + " bulan");
            if (loan.getCurrentMonthBill() != null &&
                    loan.getCurrentMonthBill().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("   Tagihan Bulan Ini : Rp " + loan.getCurrentMonthBill().getAmount());
                if ("OVERDUE".equals(status)) {
                    System.out.println("   *** Termasuk denda keterlambatan Rp 50.000 ***");
                }
            } else {
                System.out.println("   (Tagihan bulan ini belum di-generate. Silakan hubungi admin.)");
            }

        } catch (Exception e) {
            System.out.println("Gagal memuat info pinjaman: " + e.getMessage());
            return;
        }

        System.out.print("Masukkan Nominal Pembayaran (Rp): ");
        try {
            long nominalPembayaran = Long.parseLong(scanner.nextLine().trim());
            Money amount = new Money(BigDecimal.valueOf(nominalPembayaran), "IDR");

            // Panggil service untuk membayar cicilan
            ctx.getLoanService().bayarCicilan(new LoanId(inputLoanId), amount);

            // Fetch ulang loan untuk melihat status terbarunya setelah dibayar
            Loan updatedLoan = ctx.getLoanService().getLoan(new LoanId(inputLoanId));

            System.out.println("------------------------------------------");
            if ("CLOSED".equals(updatedLoan.getStatus())) {
                System.out.println("Pembayaran diterima! Selamat, Pinjaman " + inputLoanId + " telah LUNAS.");
                System.out.println("Anda sekarang bisa mengajukan pinjaman baru.");
            } else {
                System.out.println("Cicilan berhasil dibayar!");
                System.out.println("   Status Pinjaman : " + updatedLoan.getStatus());
                System.out.println("   Sisa Pokok      : Rp " + updatedLoan.getSisaTagihanKeseluruhan().getAmount());
                System.out.println("   Sisa Tenor      : " + updatedLoan.getTenorSisa() + " bulan");
                if (updatedLoan.getTanggalJatuhTempo() != null) {
                    System.out.println("   Jatuh Tempo     : " + updatedLoan.getTanggalJatuhTempo());
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: Nominal pembayaran harus berupa angka tanpa titik/koma!");
        } catch (Exception e) {
            System.out.println("Gagal membayar cicilan: " + e.getMessage());
        }
    }

    // Submenu: Simulasi Jatuh Tempo & Tandai OVERDUE
    private void menuSimulasiOverdue() {
        System.out.println("\n=== SIMULASI JATUH TEMPO & OVERDUE ===");
        System.out.println("Menu ini untuk keperluan simulasi/demo.");
        System.out.println("1. Majukan tanggal jatuh tempo ke kemarin (agar bisa ditandai OVERDUE)");
        System.out.println("2. Tandai pinjaman sebagai OVERDUE (jika sudah melewati jatuh tempo)");
        System.out.println("3. Kembali");
        System.out.print("Pilih: ");
        String sub = scanner.nextLine().trim();

        switch (sub) {
            case "1" -> {
                System.out.print("Masukkan ID Pinjaman (Loan ID): ");
                String loanIdStr = scanner.nextLine().trim();
                try {
                    ctx.getLoanService().simulasiMajukanJatuhTempo(new LoanId(loanIdStr));
                    System.out.println("Berhasil! Tanggal jatuh tempo dimajukan ke kemarin.");
                    System.out.println("Sekarang pilih opsi 2 untuk menandai pinjaman sebagai OVERDUE.");
                } catch (Exception e) {
                    System.out.println("Gagal: " + e.getMessage());
                }
            }
            case "2" -> {
                System.out.print("Masukkan ID Pinjaman (Loan ID): ");
                String loanIdStr = scanner.nextLine().trim();
                try {
                    ctx.getLoanService().tandaiOverdue(new LoanId(loanIdStr));
                    Loan loan = ctx.getLoanService().getLoan(new LoanId(loanIdStr));
                    System.out.println("Pinjaman " + loanIdStr + " sekarang berstatus OVERDUE.");
                    System.out.println("   Tagihan Bulan Ini : Rp " + loan.getCurrentMonthBill().getAmount());
                    System.out.println("   (Sudah termasuk denda keterlambatan Rp 50.000)");
                    System.out.println("   Silakan bayar cicilan sebelum denda bertambah.");
                } catch (Exception e) {
                    System.out.println("Gagal menandai OVERDUE: " + e.getMessage());
                }
            }
            case "3" -> { /* kembali */ }
            default -> System.out.println("Pilihan tidak valid.");
        }
    }
}