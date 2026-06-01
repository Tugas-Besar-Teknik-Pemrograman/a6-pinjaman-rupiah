package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.lender.ReturnRecord;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.state.LoanStateFactory;
import com.p2p.domain.valueobject.Money;
import com.p2p.application.observer.LoanEventPublisher;
import com.p2p.domain.event.PencairanBerhasilEvent;
import com.p2p.domain.event.CicilanBerhasilEvent;
import com.p2p.domain.event.PinjamanLunasEvent;
import com.p2p.domain.event.PinjamanJatuhTempoEvent;
import com.p2p.domain.event.RefundLenderEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Consumer;
import java.time.LocalDate;

public class LoanService {
    private LoanRepository loanRepository;
    private BorrowerRepository borrowerRepository;
    private LenderRepository lenderRepository;
    private NotificationService notificationService;
    private LoanEventPublisher loanEventPublisher;
    private Consumer<Money> adminFeeCallback;

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
        if (borrower == null) throw new Exception("Borrower tidak ditemukan");
        Loan loan = borrower.ajukanPinjaman(new LoanId(), amount, tenor);
        borrowerRepository.save(borrower);
        loanRepository.save(loan);
        if (loanEventPublisher != null)
            loanEventPublisher.publishPengajuanDiterima(new com.p2p.domain.event.PengajuanDiterimaEvent(loan.getId(), borrowerId));
        return loan;
    }

    public Loan ajukanPinjaman(BorrowerId borrowerId, Money amount, int tenor, String interestType) throws Exception {
        return ajukanPinjaman(borrowerId, amount, tenor, interestType, null);
    }

    public Loan ajukanPinjaman(BorrowerId borrowerId, Money amount, int tenor, String interestType, BigDecimal customRateOrMargin) throws Exception {
        Borrower borrower = borrowerRepository.findById(borrowerId);
        if (borrower == null) throw new Exception("Borrower tidak ditemukan");
        Loan loan = borrower.ajukanPinjaman(new LoanId(), amount, tenor, interestType, customRateOrMargin);
        borrowerRepository.save(borrower);
        loanRepository.save(loan);
        if (loanEventPublisher != null)
            loanEventPublisher.publishPengajuanDiterima(new com.p2p.domain.event.PengajuanDiterimaEvent(loan.getId(), borrowerId));
        return loan;
    }

    public Loan getLoan(LoanId loanId) {
        return loanRepository.findById(loanId);
    }

    public BayarCicilanResult bayarCicilan(LoanId loanId, Money amount) throws Exception {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new Exception("Loan tidak ditemukan");

        if ("REJECTED".equals(loan.getStatus()) || "CANCELED".equals(loan.getStatus())) {
            throw new IllegalStateException("Loan yang ditolak tidak bisa dibayar cicilannya");
        }

        Borrower borrower = borrowerRepository.findById(loan.getBorrowerId());
        if (borrower == null) throw new Exception("Borrower tidak ditemukan");

        Money tagihanBulanIni = loan.getTagihanBulanIni();
        if (tagihanBulanIni == null || tagihanBulanIni.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Tagihan bulan ini belum tersedia");
        }

        if (borrower.getSaldoBalance().getAmount().compareTo(tagihanBulanIni.getAmount()) < 0) {
            throw new IllegalStateException("Saldo borrower tidak mencukupi untuk membayar tagihan bulan ini");
        }

        // Bayar tidak boleh kurang dari tagihan
        if (amount.getAmount().compareTo(tagihanBulanIni.getAmount()) < 0) {
            throw new IllegalStateException("Nominal pembayaran kurang dari nominal tagihan");
        }

        // Snapshot distribusi sebelum bill di-nolkan
        Map<LenderId, Money> distribusiCicilan = loan.hitungDistribusiCicilan();

        borrower.kurangiSaldo(tagihanBulanIni);
        loan.bayarCicilan(amount);

        loanRepository.save(loan);
        borrowerRepository.save(borrower);

        if ("CLOSED".equals(loan.getStatus())) {
            borrower.setHasActiveLoan(false);
            borrowerRepository.save(borrower);
        }

        // PERBAIKAN: semua cicilan (termasuk denda) masuk ke lender, bukan admin
        if (lenderRepository != null) {
            for (Map.Entry<LenderId, Money> entry : distribusiCicilan.entrySet()) {
                Lender lender = lenderRepository.findById(entry.getKey());
                if (lender != null) {
                    lender.tambahSaldo(entry.getValue());
                    lender.tambahReturn(new ReturnRecord(loan.getId(), LocalDate.now(), entry.getValue()));
                    lenderRepository.save(lender);
                }
            }
        }

        // Fire event cicilan & lunas ke borrower
        if (loanEventPublisher != null) {
            loanEventPublisher.publishCicilanBerhasil(new CicilanBerhasilEvent(loanId, loan.getBorrowerId(), tagihanSnapshot));
            if ("CLOSED".equals(loan.getStatus())) {
                loanEventPublisher.publishPinjamanLunas(new PinjamanLunasEvent(loanId, loan.getBorrowerId()));
            }
        }

        return new BayarCicilanResult(tagihanSnapshot, amount, loan.getTenorSisa(), loan.getStatus());
    }

    public void prosesPencairan(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan tidak ditemukan");

        if (loan.getStatus().equals("FUNDING_READY")) {
            loan.cairkanPinjaman();
            Borrower borrower = borrowerRepository.findById(loan.getBorrowerId());
            if (borrower == null) throw new IllegalStateException("Borrower tidak ditemukan untuk pencairan");

            borrower.tambahSaldo(loan.getTargetNominal());

            Money adminFee = loan.getAdminFee();
            if (adminFeeCallback != null) adminFeeCallback.accept(adminFee);

            borrowerRepository.save(borrower);
            loanRepository.save(loan);

            if (loanEventPublisher != null) {
                loanEventPublisher.publishPencairanBerhasil(new PencairanBerhasilEvent(loanId, loan.getBorrowerId()));
            }
            return;
        }

        if (loan.getStatus().equals("FUNDING")) {
            if (loan.getTotalTerkumpul().getAmount().compareTo(loan.getTargetNominal().getAmount()) < 0) {
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
        if (loan == null) throw new IllegalArgumentException("Loan tidak ditemukan");
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
        if (loan == null) throw new IllegalArgumentException("Loan tidak ditemukan");
        if (!loan.getStatus().equals("FUNDING_READY")) {
            throw new IllegalStateException("Hanya pinjaman dengan status FUNDING_READY yang bisa ditolak");
        }
        if (lenderRepository != null) {
            for (Map.Entry<LenderId, Money> entry : loan.getDaftarPendana().entrySet()) {
                Lender lender = lenderRepository.findById(entry.getKey());
                if (lender != null) {
                    lender.tambahSaldo(entry.getValue());
                    lenderRepository.save(lender);
                    // Fire event refund per lender
                    if (loanEventPublisher != null) {
                        loanEventPublisher.publishRefundLender(new RefundLenderEvent(loanId, entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
        LoanStateFactory.rejected().ubahStatus(loan);
        loanRepository.save(loan);
        if (notificationService != null) {
            notificationService.kirimNotifikasi(loan.getBorrowerId(),
                "Pencairan pinjaman Anda ditolak. Alasan: " + alasan + ". Dana sudah dikembalikan ke lender.");
        }
    }

    public void tandaiOverdue(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan tidak ditemukan");
        if (!loan.isPinjamanOverdue()) {
            throw new IllegalStateException(
                "Pinjaman belum jatuh tempo atau statusnya tidak eligible untuk OVERDUE " +
                "(harus DISBURSED/REPAYMENT dan tanggal jatuh tempo sudah terlewat)");
        }
        LoanStateFactory.overdue().ubahStatus(loan);
        loan.generateMonthlyBill();
        loanRepository.save(loan);

        // Fire event jatuh tempo ke borrower
        if (loanEventPublisher != null) {
            loanEventPublisher.publishPinjamanJatuhTempo(new PinjamanJatuhTempoEvent(loanId, loan.getBorrowerId(), "Pinjaman Anda telah jatuh tempo."));
        }
    }

    public void simulasiMajukanJatuhTempo(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan tidak ditemukan");
        String status = loan.getStatus();
        if (!"DISBURSED".equals(status) && !"REPAYMENT".equals(status)) {
            throw new IllegalStateException(
                "Simulasi hanya bisa dilakukan pada status DISBURSED atau REPAYMENT, status saat ini: " + status);
        }
        loan.setTanggalJatuhTempo(LocalDate.now().minusDays(1));
        loanRepository.save(loan);
    }

    public void simulasiTenorBerikutnya(LoanId loanId) throws Exception {
        Loan loan = loanRepository.findById(loanId);
        if (loan == null) throw new IllegalArgumentException("Loan tidak ditemukan");
        String status = loan.getStatus();
        if (!"DISBURSED".equals(status) && !"REPAYMENT".equals(status) && !"OVERDUE".equals(status)) {
            throw new IllegalStateException(
                "Simulasi tenor berikutnya hanya bisa dilakukan pada status DISBURSED, REPAYMENT, atau OVERDUE. Status saat ini: " + status);
        }
        if (loan.isLunas()) throw new IllegalStateException("Pinjaman sudah lunas.");
        if (loan.getTagihanBulanIni() != null
                && loan.getTagihanBulanIni().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Harap bayar cicilan bulan ini terlebih dahulu sebelum mensimulasikan tenor berikutnya.");
        }
        loan.generateMonthlyBill();
        if (loan.getTanggalJatuhTempo() != null) {
            loan.setTanggalJatuhTempo(loan.getTanggalJatuhTempo().plusDays(30));
        }
        loanRepository.save(loan);
    }

    public void setAdminFeeCallback(Consumer<Money> callback) {
        this.adminFeeCallback = callback;
    }
}