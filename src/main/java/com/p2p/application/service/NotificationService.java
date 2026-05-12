package com.p2p.application.service;
import com.p2p.application.observer.NotificationObserver;
import com.p2p.domain.event.PencairanBerhasilEvent;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanRepository;

public class NotificationService {
    private final LoanRepository loanRepository;
    private final NotificationObserver notificationObserver;

    public NotificationService(LoanRepository loanRepository, NotificationObserver notificationObserver) {
        this.loanRepository = loanRepository;
        this.notificationObserver = notificationObserver;
    }

    public  String kirimNotifikasiPencairan(String loanId) {
        Loan loan = loanRepository.findById(loanId);
        
        if (loan == null) {
            return "Loan dengan ID " + loanId + " tidak ditemukan.";
        }

        if(!loan.isLayakNotifikasiPencairan()){
            notificationObserver.onPencairanGagal(loan.getBorrowerId(), loanId, "Syarat pencairan belum terpenuhi");
            return "Pencairan gagal: Syarat pencairan belum terpenuhi.";
        }

        PencairanBerhasilEvent event = new PencairanBerhasilEvent(loanId, loan.getBorrowerId());
        notificationObserver.onPencairanBerhasil(event);
        return "Notifikasi pencairan berhasil dikirim ke Borrower " + loan.getBorrowerId() + " untuk Loan " + loanId;
    }

    public void kirimNotifikasi(String borrowerId, String message) {
        System.out.println("Notifikasi ke Borrower " + borrowerId + ": " + message);
    }
}