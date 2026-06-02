package com.p2p.application.observer;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.event.PencairanBerhasilEvent;
import com.p2p.domain.loan.LoanId;

public class NotificationObserver {
    public void onPencairanBerhasil(PencairanBerhasilEvent event) {
        BorrowerId borrowerId = event.getBorrowerId();
        LoanId loanId = event.getLoanId();
        System.out.println("Notifikasi ke Borrower " + borrowerId + ": Dana pinjaman " + loanId + " telah berhasil dicairkan!");
    }

    public void onPencairanGagal(BorrowerId borrowerId, LoanId loanId, String reason) {
        System.out.println("Notifikasi ke Borrower " + borrowerId + ": Pencairan dana untuk pinjaman " + loanId + " gagal. Alasan: " + reason);
    }
}