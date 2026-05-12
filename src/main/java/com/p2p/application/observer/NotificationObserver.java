package com.p2p.application.observer;

import com.p2p.domain.event.PencairanBerhasilEvent;

public class NotificationObserver {
    public void onPencairanBerhasil(PencairanBerhasilEvent event) {
        String borrowerId = event.getBorrowerId();
        String loanId = event.getLoanId();
        System.out.println("Notifikasi ke Borrower" + borrowerId + ": Dana pinjaman" + loanId + " telah berhasil dicairkan!");
    }

    public void onPencairanGagal(String borrowerId, String loanId, String reason) {
        System.out.println("Notifikasi ke Borrower " + borrowerId + ": Pencairan dana untuk pinjaman " + loanId + " gagal. Alasan: " + reason);
    }
}