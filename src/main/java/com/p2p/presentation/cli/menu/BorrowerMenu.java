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
            System.out.println("1. Lihat Profil Borrower");
            System.out.println("2. Top Up Saldo");
            System.out.println("3. Ajukan Pinjaman Baru");
            System.out.println("4. Lihat Status Pinjaman");
            System.out.println("5. Bayar Cicilan");
            System.out.println("6. Status Pencairan");
            System.out.println("7. Simulasi Jatuh Tempo");
            System.out.println("8. Simulasi Tenor Selanjutnya");
            System.out.println("9. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> menuProfil();
                case "2" -> menuTopUp();
                case "3" -> menuAjukanPinjaman();
                case "4" -> menuLihatStatusPinjaman();
                case "5" -> menuBayarCicilan();
                case "6" -> menuProsesPencairan();
                case "7" -> menuSimulasiOverdue();
                case "8" -> menuSimulasiTenorSelanjutnya();
                case "9" -> {
                    ctx.logout();
                    System.out.println("Logout berhasil.");
                    kembali = true;
                }
                default -> System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private void menuProfil() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) {
            System.out.println("Gagal, Profil Borrower tidak ditemukan.");
            return;
        }

        try {
            Borrower borrower = ctx.getRepos().getBorrowerRepository().findById(new BorrowerId(borrowerIdStr));
            com.p2p.domain.user.User user = ctx.getRepos().getUserRepository().findById(new com.p2p.domain.user.UserId(ctx.getCurrentUserId()));
            if (borrower == null || user == null) {
                System.out.println("Gagal, data Borrower/User tidak ditemukan.");
                return;
            }

            System.out.println("\n=== PROFIL BORROWER ===");
            System.out.println("ID Borrower        : " + borrower.getId().getValue());
            System.out.println("Nama               : " + user.getNama());
            System.out.println("Email              : " + user.getEmail());
            System.out.println("Usia               : " + user.getUsia() + " tahun");
            System.out.println("Penghasilan Bulanan: Rp " + borrower.getPenghasilan().getAmount());
            System.out.println("Limit Pinjaman Awal: Rp " + borrower.getLimitPinjaman().getAmount() + " (30% dari Penghasilan)");
            System.out.println("Catatan Limit      : Limit pengajuan riil dihitung dinamis berdasarkan tenor & jenis bunga");
            System.out.println("Status KYC         : " + (borrower.isKycStatus() ? "Terverifikasi" : "Belum Terverifikasi"));
            System.out.println("Credit Score       : " + borrower.getCreditScore());
            System.out.println("Pinjaman Aktif     : " + (borrower.hasActiveLoan() ? "Ada" : "Tidak Ada"));
            System.out.println("Saldo Saat Ini     : Rp " + borrower.getSaldoBalance().getAmount());
            System.out.println("=======================");
        } catch (Exception e) {
            System.out.println("Gagal memuat profil: " + e.getMessage());
        }
    }

    private void menuTopUp() {
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

            System.out.println("\n--- Top Up Saldo Borrower ---");
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
            if (borrowerObj == null) {
                System.out.println("Gagal, data Borrower tidak ditemukan.");
                return;
            }

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
                default -> throw new IllegalArgumentException("Pilihan bunga tidak valid!");
            };

            BigDecimal nominalBigDecimal = BigDecimal.valueOf(nominal);
            
            // Lakukan pre-validasi sebelum menampilkan detail preview
            if (!borrowerObj.isKycStatus()) {
                throw new IllegalStateException("Peminjaman ditolak karena Borrower belum terverifikasi (KYC)");
            }
            if (borrowerObj.getCreditScore() < 600) {
                throw new IllegalStateException("Peminjaman ditolak karena Credit score di bawah ambang batas");
            }
            if (nominalBigDecimal.compareTo(new BigDecimal("100000")) < 0) {
                throw new IllegalArgumentException("Nominal pinjaman harus lebih dari 100.000");
            }
            if (borrowerObj.hasActiveLoan()) {
                throw new IllegalStateException("Lunasi Peminjaman sebelumnya dulu");
            }

            BigDecimal bungaRate = BigDecimal.ZERO;
            if ("flat".equalsIgnoreCase(interestType) || "fixed".equalsIgnoreCase(interestType)) {
                bungaRate = new BigDecimal("0.05");
            } else if ("float".equalsIgnoreCase(interestType) || "floating".equalsIgnoreCase(interestType)) {
                bungaRate = new BigDecimal("0.05");
            }
            
            Money limitDinamis = borrowerObj.hitungLimitDenganTenorDanBunga(tenor, bungaRate);
            Money requestedAmount = new Money(nominalBigDecimal, "IDR");
            if (limitDinamis.isLessThan(requestedAmount)) {
                throw new IllegalStateException("Pengajuan melebihi limit dinamis untuk tenor " + tenor + " bulan. Limit Anda adalah Rp " + limitDinamis.getAmount());
            }

            // Jika lolos pre-validasi, hitung rincian simulasi cicilan
            BigDecimal principalPerMonth = nominalBigDecimal.divide(BigDecimal.valueOf(tenor), 2, RoundingMode.HALF_UP);
            BigDecimal totalInterest = BigDecimal.ZERO;
            BigDecimal adminFee = nominalBigDecimal.multiply(new BigDecimal("0.01")).setScale(0, RoundingMode.HALF_UP); // 1% admin fee
            
            System.out.println("\n=================================================");
            System.out.println("            PREVIEW PENGAJUAN PINJAMAN           ");
            System.out.println("=================================================");
            System.out.printf("Nominal Pinjaman : Rp %,.0f%n", nominalBigDecimal);
            System.out.printf("Tenor            : %d Bulan%n", tenor);
            System.out.printf("Jenis Bunga      : %s%n", interestType.toUpperCase());
            System.out.printf("Biaya Admin (1%%) : Rp %,.0f (dipotong saat pencairan)%n", adminFee);
            System.out.println("-------------------------------------------------");
            System.out.println("           Simulasi Cicilan Bulanan              ");
            System.out.println("-------------------------------------------------");
            
            BigDecimal sisaPokok = nominalBigDecimal;
            BigDecimal totalPengembalian = BigDecimal.ZERO;
            
            for (int i = 1; i <= tenor; i++) {
                BigDecimal bungaAtauMargin = BigDecimal.ZERO;
                if ("flat".equals(interestType)) {
                    bungaAtauMargin = nominalBigDecimal.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
                } else if ("float".equals(interestType)) {
                    bungaAtauMargin = sisaPokok.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
                } else if ("syariah".equals(interestType)) {
                    bungaAtauMargin = new BigDecimal("150000");
                }
                
                BigDecimal totalCicilanBulanIni = principalPerMonth.add(bungaAtauMargin);
                totalPengembalian = totalPengembalian.add(totalCicilanBulanIni);
                totalInterest = totalInterest.add(bungaAtauMargin);
                
                System.out.printf("Bulan %2d: Pokok Rp %,.0f + %s Rp %,.0f = Cicilan Rp %,.0f%n", 
                        i, 
                        principalPerMonth, 
                        "syariah".equals(interestType) ? "Margin" : "Bunga", 
                        bungaAtauMargin, 
                        totalCicilanBulanIni);
                
                sisaPokok = sisaPokok.subtract(principalPerMonth);
            }
            
            System.out.println("-------------------------------------------------");
            System.out.printf("Total Bunga/Margin : Rp %,.0f%n", totalInterest);
            System.out.printf("Total Pengembalian : Rp %,.0f%n", totalPengembalian);
            System.out.println("=================================================");
            System.out.print("Apakah Anda setuju dengan rincian di atas? (y/n): ");
            String persetujuan = scanner.nextLine().trim();
            if (!"y".equalsIgnoreCase(persetujuan)) {
                System.out.println("Pengajuan pinjaman dibatalkan. Kembali ke menu.");
                return;
            }

            Loan loan = ctx.getLoanService().ajukanPinjaman(new BorrowerId(borrowerIdStr), requestedAmount, tenor, interestType);
            System.out.println("Berhasil! ID: " + loan.getId().getValue());
            
            System.out.println("\n=================================================");
            System.out.println("            RINCIAN BIAYA ADMINISTRASI           ");
            System.out.println("=================================================");
            System.out.printf("Nominal Pinjaman : Rp %,.0f%n", nominalBigDecimal);
            System.out.printf("Biaya Admin (1%%) : Rp %,.0f (dipotong saat pencairan)%n", loan.getAdminFee().getAmount());
            System.out.printf("Estimasi Bersih  : Rp %,.0f%n", nominalBigDecimal.subtract(loan.getAdminFee().getAmount()));
            System.out.println("=================================================");
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

        // Tampilkan info status pinjaman yang FUNDING_READY
        List<Loan> allLoans = ctx.getRepos().getLoanRepository().findAll();
        List<Loan> readyLoans = allLoans.stream()
                .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                .filter(l -> "FUNDING_READY".equals(l.getStatus()))
                .toList();

        System.out.println("\n--- Status Pencairan Pinjaman ---");
        if (readyLoans.isEmpty()) {
            System.out.println("Tidak ada pinjaman Anda yang sedang menunggu pencairan.");
        } else {
            System.out.println("Pinjaman berikut sudah siap dan menunggu persetujuan pencairan dari Admin:");
            for (Loan l : readyLoans) {
                System.out.printf("  - Loan ID : %s%n", l.getId().getValue());
                System.out.printf("    Nominal : Rp %s%n", l.getTargetNominal().getAmount());
                System.out.printf("    Status  : %s%n", l.getStatus());
            }
            System.out.println();
            System.out.println("[INFO] Pencairan hanya dapat dilakukan oleh Admin.");
            System.out.println("    Silakan hubungi Admin untuk memproses pencairan dana Anda.");
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

    private void menuSimulasiTenorSelanjutnya() {
        String borrowerIdStr = ctx.getBorrowerId(ctx.getCurrentUserId());
        if (borrowerIdStr == null) {
            System.out.println("Gagal, Profil Borrower tidak ditemukan.");
            return;
        }

        List<Loan> allLoans = ctx.getRepos().getLoanRepository().findAll();
        List<Loan> activeLoans = allLoans.stream()
                .filter(l -> l.getBorrowerId() != null && borrowerIdStr.equals(l.getBorrowerId().getValue()))
                .filter(l -> "DISBURSED".equals(l.getStatus()) || "REPAYMENT".equals(l.getStatus()) || "OVERDUE".equals(l.getStatus()))
                .toList();

        if (activeLoans.isEmpty()) {
            System.out.println("Anda tidak memiliki pinjaman aktif yang sedang berjalan.");
            return;
        }

        Loan selectedLoan;
        if (activeLoans.size() == 1) {
            selectedLoan = activeLoans.get(0);
            System.out.println("Menemukan 1 pinjaman aktif: " + selectedLoan.getId().getValue() + ". Memproses simulasi tenor berikutnya...");
        } else {
            System.out.println("\n--- Pinjaman Aktif Anda ---");
            for (int i = 0; i < activeLoans.size(); i++) {
                Loan l = activeLoans.get(i);
                System.out.printf("%d) %s - Tagihan Bulan Ini: Rp %s - Sisa Tenor: %d bulan%n", 
                        i + 1, l.getId().getValue(), 
                        (l.getTagihanBulanIni() != null ? l.getTagihanBulanIni().getAmount() : "0"),
                        l.getTenorSisa());
            }
            System.out.print("Pilih nomor pinjaman untuk disimulasikan (atau '0' untuk batal): ");
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) return;
            try {
                int idx = Integer.parseInt(choice) - 1;
                if (idx < 0 || idx >= activeLoans.size()) {
                    System.out.println("Pilihan tidak valid.");
                    return;
                }
                selectedLoan = activeLoans.get(idx);
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid.");
                return;
            }
        }

        try {
            ctx.getLoanService().simulasiTenorBerikutnya(selectedLoan.getId());
            Loan updated = ctx.getRepos().getLoanRepository().findById(selectedLoan.getId());
            System.out.println("Simulasi sukses! Tagihan baru untuk bulan selanjutnya telah dibuat.");
            System.out.println("Tagihan Bulan Ini  : Rp " + (updated.getTagihanBulanIni() != null ? updated.getTagihanBulanIni().getAmount() : "0"));
            System.out.println("Sisa Tenor         : " + updated.getTenorSisa() + " bulan");
            System.out.println("Tanggal Jatuh Tempo: " + updated.getTanggalJatuhTempo());
        } catch (Exception e) {
            System.out.println("Gagal mensimulasikan tenor berikutnya: " + e.getMessage());
        }
    }
}