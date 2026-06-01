package com.p2p.presentation.cli.menu;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.user.User;
import com.p2p.domain.valueobject.Money;
import com.p2p.infrastructure.memory.RepositoryFactory;
import com.p2p.presentation.cli.AppContext;
import com.p2p.application.service.LoanService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
            System.out.println("5. Validasi Pencairan Pinjaman");
            System.out.println("6. Laporan Statistik Platform");
            System.out.println("7. Saldo Platform");
            System.out.println("8. Simulasi Jatuh Tempo");
            System.out.println("9. Simulasi Tenor Selanjutnya");
            System.out.println("10. Logout");
            System.out.print("Pilih: ");
            String pilihan = scanner.nextLine().trim();

            switch (pilihan) {
                case "1" -> lihatSemuaPengguna();
                case "2" -> approveKycBorrower();
                case "3" -> approveKycLender();
                case "4" -> lihatSemuaPinjaman();
                case "5" -> validasiPencairan();
                case "6" -> laporanStatistikPlatform();
                case "7" -> lihatSaldoPlatform();
                case "8" -> menuSimulasiOverdue();
                case "9" -> menuSimulasiTenorSelanjutnya();
                case "10" -> {
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
        System.out.printf("%-30s %-20s %-25s %-10s %-15s%n", "ID", "Nama", "Email", "Role", "Status");
        System.out.println("-".repeat(105));
        for (User u : users) {
            String status = getStatusKeterangan(u);
            System.out.printf("%-30s %-20s %-25s %-10s %-15s%n",
                    u.getId(), u.getNama(), u.getEmail(), getRoleString(u.getRole()), status);
        }
    }

    private String getStatusKeterangan(User u) {
        String userId = u.getId().getValue();
        
        // Role 1 = Borrower
        if (u.getRole() == 1) {
            Borrower borrower = repos.getBorrowerRepository().findById(new BorrowerId(userId));
            if (borrower != null) {
                return borrower.isKycStatus() ? "APPROVED" : "PENDING";
            }
            return "PENDING";
        }
        // Role 2 = Lender
        else if (u.getRole() == 2) {
            Lender lender = repos.getLenderRepository().findById(new LenderId(userId));
            if (lender != null) {
                return lender.isKycVerified() ? "APPROVED" : "PENDING";
            }
            return "PENDING";
        }
        // Role 3 = Admin (tidak ada status KYC)
        else if (u.getRole() == 3) {
            return "ADMIN";
        }
        return "N/A";
    }

    private String getRoleString(int role) {
        return switch (role) {
            case 1 -> "BORROWER";
            case 2 -> "LENDER";
            case 3 -> "ADMIN";
            default -> "UNKNOWN";
        };
    }

    private void approveKycBorrower() {
        System.out.print("Masukkan Borrower ID: ");
        String idInput = scanner.nextLine().trim();
        Borrower borrower = repos.getBorrowerRepository().findById(new BorrowerId(idInput));
        if (borrower == null) {
            System.out.println("Borrower tidak ditemukan.");
            return;
        }

        // Input credit score
        System.out.print("Masukkan Credit Score : ");
        int creditScore;
        try {
            creditScore = Integer.parseInt(scanner.nextLine().trim());
            if (creditScore < 0) {
                System.out.println("Credit score tidak boleh negatif.");
                return;
}
        } catch (NumberFormatException e) {
            System.out.println("Input credit score tidak valid.");
            return;
        }

        borrower.setKycStatus(true);
        borrower.setCreditScore(creditScore);
        repos.getBorrowerRepository().save(borrower);
        System.out.println("KYC Borrower " + idInput + " berhasil di-approve dengan Credit Score: " + creditScore);
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
        System.out.printf("%-30s %-20s %-25s %-15s%n", "Loan ID", "Nominal", "Estimasi Admin Fee (1%)", "Status");
        System.out.println("-".repeat(95));
        for (Loan l : loans) {
            System.out.printf("%-30s %-20s %-25s %-15s%n",
                    l.getId(), 
                    "Rp " + formatCurrency(l.getTargetNominal().getAmount()), 
                    "Rp " + formatCurrency(l.getAdminFee().getAmount()), 
                    l.getStatus());
        }
    }

    private void validasiPencairan() {
        List<Loan> allLoans = repos.getLoanRepository().findAll();
        List<Loan> fundingReadyLoans = allLoans.stream()
                .filter(l -> "FUNDING_READY".equals(l.getStatus()))
                .toList();

        if (fundingReadyLoans.isEmpty()) {
            System.out.println("Tidak ada pinjaman dengan status FUNDING_READY.");
            return;
        }

        System.out.println("\n--- Daftar Pinjaman Siap Pencairan (FUNDING_READY) ---");
        System.out.printf("%-3s %-30s %-20s %-20s%n", "No", "Loan ID", "Nominal", "Terkumpul");
        System.out.println("-".repeat(80));
        for (int i = 0; i < fundingReadyLoans.size(); i++) {
            Loan l = fundingReadyLoans.get(i);
            System.out.printf("%-3d %-30s %-20s %-20s%n",
                    i + 1, l.getId(), l.getTargetNominal().getAmount(), l.getTotalTerkumpul().getAmount());
        }

        System.out.print("Pilih no pinjaman (atau '0' untuk batal): ");
        String choice = scanner.nextLine().trim();
        if ("0".equals(choice)) {
            return;
        }

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index < 0 || index >= fundingReadyLoans.size()) {
                System.out.println("Pilihan tidak valid.");
                return;
            }
            Loan selectedLoan = fundingReadyLoans.get(index);

            System.out.println("\n--- Pilihan Aksi ---");
            System.out.println("1. Terima (Accept) - Dana dicairkan");
            System.out.println("2. Tolak (Decline) - Refund ke lender");
            System.out.print("Pilih: ");
            String aksi = scanner.nextLine().trim();

            if ("1".equals(aksi)) {
                // Accept pencairan
                LoanService loanService = ctx.getLoanService();
                long maturityDate = System.currentTimeMillis() + (28L * 24 * 60 * 60 * 1000); // +28 hari
                selectedLoan.setTanggalJatuhTempoTimestemp(maturityDate);

                Money feePreview = selectedLoan.getTargetNominal().multiply(new BigDecimal("0.01"));

                loanService.prosesPencairan(selectedLoan.getId());
                System.out.println("Pencairan pinjaman " + selectedLoan.getId() + " berhasil disetujui!");
                System.out.println("Tanggal jatuh tempo: " + new java.util.Date(maturityDate));
                System.out.println("Admin fee 1% dipotong: Rp" + formatCurrency(feePreview.getAmount()));
                System.out.println("Saldo Platform saat ini: Rp" + formatCurrency(ctx.getAdminSaldo().getAmount()));
            } else if ("2".equals(aksi)) {
                // Decline pencairan
                System.out.print("Masukkan alasan penolakan: ");
                String alasan = scanner.nextLine().trim();
                if (alasan.isEmpty()) {
                    System.out.println("Alasan tidak boleh kosong.");
                    return;
                }
                
                LoanService loanService = ctx.getLoanService();
                loanService.menolakPencairan(selectedLoan.getId(), alasan);
                System.out.println("Pencairan pinjaman " + selectedLoan.getId() + " berhasil ditolak.");
                System.out.println("Dana sudah dikembalikan ke lender.");
            } else {
                System.out.println("Pilihan tidak valid.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Input tidak valid.");
        }
    }

    private void laporanStatistikPlatform() {
        List<User> allUsers = repos.getUserRepository().findAll();
        List<Loan> allLoans = repos.getLoanRepository().findAll();
        List<Borrower> allBorrowers = repos.getBorrowerRepository().findAll();
        List<Lender> allLenders = repos.getLenderRepository().findAll();

        // Calculate metrics
        long totalUsers = allUsers.size();
        long borrowerCount = allUsers.stream().filter(u -> u.getRole() == 1).count();
        long lenderCount = allUsers.stream().filter(u -> u.getRole() == 2).count();
        long adminCount = allUsers.stream().filter(u -> u.getRole() == 3).count();

        BigDecimal totalDisbursed = allLoans.stream()
                .filter(l -> "DISBURSED".equals(l.getStatus()) || "REPAYMENT".equals(l.getStatus()) || "CLOSED".equals(l.getStatus()))
                .map(l -> l.getTargetNominal().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstandingPrincipal = allLoans.stream()
                .filter(l -> "REPAYMENT".equals(l.getStatus()))
                .map(l -> l.getSisaPokok().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fundingInProgress = allLoans.stream()
                .filter(l -> "FUNDING".equals(l.getStatus()) || "FUNDING_READY".equals(l.getStatus()))
                .map(l -> l.getTotalTerkumpul().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueCount = allLoans.stream().filter(l -> "OVERDUE".equals(l.getStatus())).count();
        long totalLoans = allLoans.size();
        double overdueRatio = totalLoans > 0 ? (double) overdueCount / totalLoans * 100 : 0;

        BigDecimal totalLenderBalance = allLenders.stream()
                .map(l -> l.getSaldoBalance().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInvestedAmount = allLoans.stream()
                .map(l -> l.getTotalTerkumpul().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOverdueFees = allLoans.stream()
                .map(l -> l.getTotalDendaTerkumpul().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Display Laporan
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                    LAPORAN STATISTIK PLATFORM P2P LENDING");
        System.out.println("=".repeat(80));

        System.out.println("\n[STATISTIK] DATA PENGGUNA:");
        System.out.println("  - Total Pengguna Terdaftar: " + totalUsers);
        System.out.println("  - Total Borrower: " + borrowerCount);
        System.out.println("  - Total Lender: " + lenderCount);
        System.out.println("  - Total Admin: " + adminCount);

        System.out.println("\n[FINANSIAL] DATA DANA & PINJAMAN:");
        System.out.println("  - Total Pinjaman Disalurkan: Rp" + formatCurrency(totalDisbursed));
        System.out.println("  - Outstanding Principal (Dana Aktif Dipinjam): Rp" + formatCurrency(outstandingPrincipal));
        System.out.println("  - Dana dalam Proses Funding: Rp" + formatCurrency(fundingInProgress));

        System.out.println("\n[PERINGATAN] INDIKATOR KESEHATAN KREDIT:");
        System.out.println("  - Jumlah Pinjaman OVERDUE: " + overdueCount + " dari " + totalLoans);
        System.out.println("  - Rasio Keterlambatan: " + String.format("%.2f%%", overdueRatio));
        System.out.println("  - Total Denda Terkumpul: Rp" + formatCurrency(totalOverdueFees));

        System.out.println("\n[INVESTASI] DATA INVESTASI LENDER:");
        System.out.println("  - Total Saldo Lender Terkumpul: Rp" + formatCurrency(totalLenderBalance));
        System.out.println("  - Total Dana yang Sudah Diinvestasikan: Rp" + formatCurrency(totalInvestedAmount));

        System.out.println("\n[PLATFORM] SALDO PLATFORM:");
        Money adminSaldo = ctx.getAdminSaldo();
        System.out.println("  - Saldo Platform (Admin Fee 1%): Rp" + formatCurrency(adminSaldo.getAmount()));

        System.out.println("\n" + "=".repeat(80));
    }

    private void lihatSaldoPlatform() {
        Money adminSaldo = ctx.getAdminSaldo();
        List<Loan> allLoans = repos.getLoanRepository().findAll();
        
        // Calculate total admin fees collected
        BigDecimal totalAdminFees = BigDecimal.ZERO;
        long totalLoansDisbursed = 0;
        
        for (Loan loan : allLoans) {
            if ("DISBURSED".equals(loan.getStatus()) || "REPAYMENT".equals(loan.getStatus()) || "CLOSED".equals(loan.getStatus())) {
                totalLoansDisbursed++;
                Money adminFee = loan.getTargetNominal().multiply(new BigDecimal("0.01"));
                totalAdminFees = totalAdminFees.add(adminFee.getAmount());
            }
        }
        
        // Display Saldo Platform
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        SALDO PLATFORM SAAT INI");
        System.out.println("=".repeat(80));
        
        System.out.println("\n[INFO] INFORMASI SALDO PLATFORM:");
        System.out.println("  - Saldo Saat Ini: Rp" + formatCurrency(adminSaldo.getAmount()));
        System.out.println("  - Mata Uang: " + adminSaldo.getCurrency());
        
        System.out.println("\n[STATISTIK] STATISTIK ADMIN FEE:");
        System.out.println("  - Total Pinjaman Dicairkan: " + totalLoansDisbursed);
        System.out.println("  - Total Admin Fee Terkumpul (1%): Rp" + formatCurrency(totalAdminFees));
        
        // Calculate average fee per loan
        if (totalLoansDisbursed > 0) {
            BigDecimal avgFee = totalAdminFees.divide(new BigDecimal(totalLoansDisbursed), 2, java.math.RoundingMode.HALF_UP);
            System.out.println("  - Rata-rata Fee per Pinjaman: Rp" + formatCurrency(avgFee));
        }
        
        System.out.println("\n" + "=".repeat(80));
    }

    private String formatCurrency(BigDecimal amount) {
        return amount.toString().replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",");
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
    List<Loan> allLoans = repos.getLoanRepository().findAll();
    List<Loan> activeLoans = allLoans.stream()
            .filter(l -> "DISBURSED".equals(l.getStatus()) || "REPAYMENT".equals(l.getStatus()) || "OVERDUE".equals(l.getStatus()))
            .toList();

    if (activeLoans.isEmpty()) {
        System.out.println("Tidak ada pinjaman aktif yang sedang berjalan.");
        return;
    }

    Loan selectedLoan;
    if (activeLoans.size() == 1) {
        selectedLoan = activeLoans.get(0);
        System.out.println("Menemukan 1 pinjaman aktif: " + selectedLoan.getId().getValue() + ". Memproses simulasi tenor berikutnya...");
    } else {
        System.out.println("\n--- Pinjaman Aktif ---");
        for (int i = 0; i < activeLoans.size(); i++) {
            Loan l = activeLoans.get(i);
            System.out.printf("%d) %s - Tagihan Bulan Ini: Rp %s - Sisa Tenor: %d bulan%n",
                    i + 1, l.getId().getValue(),
                    (l.getTagihanBulanIni() != null ? l.getTagihanBulanIni().getAmount() : "0"),
                    l.getTenorSisa());
        }
        System.out.print("Pilih nomor pinjaman (atau '0' untuk batal): ");
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
        Loan updated = repos.getLoanRepository().findById(selectedLoan.getId());
        System.out.println("Simulasi sukses! Tagihan baru untuk bulan selanjutnya telah dibuat.");
        System.out.println("Tagihan Bulan Ini  : Rp " + (updated.getTagihanBulanIni() != null ? updated.getTagihanBulanIni().getAmount() : "0"));
        System.out.println("Sisa Tenor         : " + updated.getTenorSisa() + " bulan");
        System.out.println("Tanggal Jatuh Tempo: " + updated.getTanggalJatuhTempo());
    } catch (Exception e) {
        System.out.println("Gagal mensimulasikan tenor berikutnya: " + e.getMessage());
    }
}

}
