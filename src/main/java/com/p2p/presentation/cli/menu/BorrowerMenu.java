package com.p2p.presentation.cli.menu;

import com.p2p.presentation.cli.AppContext;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.valueobject.Money;

import java.math.BigDecimal;
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
            System.out.println("4. Logout");
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

                case "2" -> System.out.println("Fitur ini belum diimplementasi.");

                case "3" -> {
                    System.out.println("\nBAYAR CICILAN");
                    System.out.print("Masukkan ID Pinjaman (Loan ID): ");
                    String inputLoanId = scanner.nextLine().trim();

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
                            System.out
                                    .println("Pembayaran diterima! Selamat, Pinjaman " + inputLoanId + " telah LUNAS.");
                        } else {
                            System.out.println("Cicilan berhasil dibayar!");
                            System.out.println("   Status Pinjaman : " + updatedLoan.getStatus());
                            System.out.println(
                                    "   Sisa Pokok      : Rp " + updatedLoan.getSisaTagihanKeseluruhan().getAmount());
                        }
                        System.out.println("------------------------------------------");

                    } catch (NumberFormatException e) {
                        System.out.println("Error: Nominal pembayaran harus berupa angka tanpa titik/koma!");
                    } catch (Exception e) {
                        System.out.println("Gagal membayar cicilan: " + e.getMessage());
                    }
                }

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