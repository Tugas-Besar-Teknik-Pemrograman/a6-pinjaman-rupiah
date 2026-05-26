package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.state.LoanStateFactory;
import com.p2p.domain.valueobject.Money;
import com.p2p.application.observer.LoanEventPublisher;
import com.p2p.domain.event.PencairanBerhasilEvent;
import java.util.Map;

public class LoanService {
    private LoanRepository loanRepository;
    private BorrowerRepository borrowerRepository;
    private LenderRepository lenderRepository;
    private NotificationService notificationService;
    private LoanEventPublisher loanEventPublisher;

    public LoanService() {}

    public LoanService(LoanRepository loanRepository, BorrowerRepository borrowerRepository, LoanEventPublisher loanEventPublisher, NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.loanEventPublisher = loanEventPublisher;
        this.notificationService = notificationService;
    }

    public LoanService(LoanRepository loanRepository, BorrowerRepository borrowerRepository, LenderRepository lenderRepository, LoanEventPublisher loanEventPublisher, NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.lenderRepository = lenderRepository;
        this.loanEventPublisher = loanEventPublisher;
        this.notificationService = notificationService;
    }

    public LoanService(LoanRepository loanRepository, BorrowerRepository borrowerRepository, NotificationService notificationService) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.notificationService = notificationService;
    }

    public Loan ajukanPinjaman(BorrowerId borrowerId, Money amount, int tenor) throws Exception {
        Borrower borrower = borrowerRepository.findById(borrowerId);
        if (borrower == null) {
            throw new Exception("Borrower tidak ditemukan");
        }
        Loan loan = borrower.ajukanPinjaman(new LoanId(), amount, tenor);
        borrowerRepository.save(borrower);
        loanRepository.save(loan);
        return loan;
    }

    public Loan ajukanPinjaman(BorrowerId borrowerId, Money amount, int tenor, String interestType) throws Exception {
        return ajukanPinjaman(borrowerId, amount, tenor, interestType, null);
    }

    public Loan ajukanPinjaman(BorrowerId borrowerId, Money amount, int tenor, String interestType, java.math.BigDecimal customRateOrMargin) throws Exception {
        Borrower borrower = borrowerRepository.findById(borrowerId);
        if (borrower == null) {
            throw new Exception("Borrower tidak ditemukan");
        }
        Loan loan = borrower.ajukanPinjaman(new LoanId(), amount, tenor, interestType, customRateOrMargin);
        borrowerRepository.save(borrower);
        loanRepository.save(loan);
        return loan;
    }

    public Loan getLoan(LoanId loanId) {
        return loanRepository.findById(loanId);
    }

    public void bayarCicilan(LoanId loanId, Money amount) throws Exception {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new Exception("Loan tidak ditemukan");
        }

        // Pendelegasian ke entitas Domain.
        // Segala validasi denda overdue, perubahan status lunas (CLOSED),
        // atau kurang bayar, akan di-handle di dalam method ini.
        loan.bayarCicilan(amount);

        loanRepository.save(loan);

        // [FIX BUG] Jika pinjaman sudah lunas, update hasActiveLoan
        // borrower menjadi false agar borrower bisa mengajukan pinjaman baru.
        if ("CLOSED".equals(loan.getStatus())) {
            Borrower borrower = borrowerRepository.findById(loan.getBorrowerId());
            if (borrower != null) {
                borrower.setHasActiveLoan(false);
                borrowerRepository.save(borrower);
            }
        }
    }

    public void prosesPencairan(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan tidak ditemukan");
        }
        if (loan.getStatus().equals("FUNDING_READY")) {
            LoanStateFactory.disbursed().ubahStatus(loan);
            loanRepository.save(loan);
            if (loanEventPublisher != null) {
               loanEventPublisher.publishPencairanBerhasil(new PencairanBerhasilEvent(loanId, loan.getBorrowerId()));
            }
            return;
        }
        if (loan.getStatus().equals("FUNDING")) {
            Money terkumpul = loan.getTotalTerkumpul();
            Money target = loan.getTargetNominal();
            if (terkumpul.getAmount().compareTo(target.getAmount()) < 0) {
                throw new IllegalStateException("Pencairan ditolak, pendanaan belum terpenuhi");
            }
            LoanStateFactory.fundingReady().ubahStatus(loan);
            loanRepository.save(loan);
            return;
        }
        throw new IllegalStateException("Status loan tidak valid untuk pencairan");
    }

    public String kirimNotifikasiPencairan(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan tidak ditemukan");
        }
        BorrowerId borrowerId = loan.getBorrowerId();
        if (loan.getStatus().equals("DISBURSED")) {
            notificationService.kirimNotifikasi(borrowerId, "Dana berhasil dicairkan");
            return "berhasil";
        }
        notificationService.kirimNotifikasi(borrowerId, "Pencairan gagal: pendanaan belum terpenuhi");
        return "gagal";
    }

    public void menolakPencairan(LoanId loanId, String alasan) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan tidak ditemukan");
        }
        if (!loan.getStatus().equals("FUNDING_READY")) {
            throw new IllegalStateException("Hanya pinjaman dengan status FUNDING_READY yang bisa ditolak");
        }
        
        // Refund ke semua lender
        Map<LenderId, Money> daftarPendana = loan.getDaftarPendana();
        if (lenderRepository != null) {
            for (Map.Entry<LenderId, Money> entry : daftarPendana.entrySet()) {
                LenderId lenderId = entry.getKey();
                Money refundAmount = entry.getValue();
                Lender lender = lenderRepository.findById(lenderId);
                if (lender != null) {
                    lender.tambahSaldo(refundAmount);
                    lenderRepository.save(lender);
                }
            }
        }
        
        // Update status loan menjadi CANCELLED
        LoanStateFactory.cancelled().ubahStatus(loan);
        loanRepository.save(loan);
        
        // Notifikasi borrower
        if (notificationService != null) {
            notificationService.kirimNotifikasi(loan.getBorrowerId(), 
                "Pencairan pinjaman Anda ditolak. Alasan: " + alasan + ". Dana sudah dikembalikan ke lender.");
        }
    }

    /**
     * Menandai sebuah pinjaman sebagai OVERDUE jika sudah melewati tanggal jatuh tempo.
     * Dipanggil secara manual dari CLI (menu simulasi) atau bisa dijadwalkan secara otomatis.
     *
     * @throws IllegalStateException jika loan tidak ditemukan, statusnya tidak eligible,
     *                                atau tanggal jatuh tempo belum terlewat.
     */
    public void tandaiOverdue(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan tidak ditemukan");
        }
        // isPinjamanOverdue() sudah melakukan cek status (DISBURSED/REPAYMENT) dan tanggal
        if (!loan.isPinjamanOverdue()) {
            throw new IllegalStateException(
                "Pinjaman belum jatuh tempo atau statusnya tidak eligible untuk OVERDUE " +
                "(harus DISBURSED/REPAYMENT dan tanggal jatuh tempo sudah terlewat)"
            );
        }
        LoanStateFactory.overdue().ubahStatus(loan);
        // Regenerasi tagihan agar denda Rp50.000 ikut terhitung
        loan.generateMonthlyBill();
        loanRepository.save(loan);
    }

    /**
     * Memajukan tanggal jatuh tempo pinjaman ke hari kemarin untuk keperluan
     * simulasi CLI, sehingga pinjaman langsung bisa ditandai OVERDUE.
     * Hanya boleh dipanggil pada lingkungan simulasi/testing.
     
     * @throws IllegalArgumentException jika loan tidak ditemukan.
     * @throws IllegalStateException    jika status bukan DISBURSED atau REPAYMENT.
     */
    public void simulasiMajukanJatuhTempo(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan tidak ditemukan");
        }
        String status = loan.getStatus();
        if (!"DISBURSED".equals(status) && !"REPAYMENT".equals(status)) {
            throw new IllegalStateException(
                "Simulasi hanya bisa dilakukan pada status DISBURSED atau REPAYMENT, " +
                "status saat ini: " + status
            );
        }
        // Set tanggal jatuh tempo ke kemarin agar isPinjamanOverdue() = true
        loan.setTanggalJatuhTempo(LocalDate.now().minusDays(1));
        loanRepository.save(loan);
    }
}