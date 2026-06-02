package com.p2p.application.service;

import com.p2p.application.observer.BorrowerNotificationObserver;
import com.p2p.domain.event.PencairanBerhasilEvent;
import com.p2p.domain.event.PencairanGagalEvent;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationService {
    private final LoanRepository loanRepository;
    private final BorrowerNotificationObserver notificationObserver;

    // Storage notifikasi per userId (borrowerId / lenderId value)
    private final Map<String, List<String>> kotakNotifikasi = new HashMap<>();

    public NotificationService(LoanRepository loanRepository, BorrowerNotificationObserver notificationObserver) {
        this.loanRepository = loanRepository;
        this.notificationObserver = notificationObserver;
    }

    public String kirimNotifikasiPencairan(LoanId loanId) {
        Loan loan = loanRepository.findById(loanId);

        if (loan == null) {
            return "Loan dengan ID " + loanId + " tidak ditemukan.";
        }

        if (!loan.isLayakNotifikasiPencairan()) {
            PencairanGagalEvent gagalEvent = new PencairanGagalEvent(loanId, loan.getBorrowerId(), "Syarat pencairan belum terpenuhi");
            notificationObserver.onPencairanGagal(gagalEvent);
            return "Pencairan gagal: Syarat pencairan belum terpenuhi.";
        }

        PencairanBerhasilEvent event = new PencairanBerhasilEvent(loanId, loan.getBorrowerId());
        notificationObserver.onPencairanBerhasil(event);
        return "Notifikasi pencairan berhasil dikirim ke Borrower " + loan.getBorrowerId() + " untuk Loan " + loanId;
    }

    public void kirimNotifikasi(BorrowerId borrowerId, String message) {
        System.out.println("Notifikasi ke Borrower " + borrowerId + ": " + message);
        // Simpan ke kotak notifikasi agar dapat diakses borrower di menu
        simpanNotifikasi(borrowerId.getValue(), message);
    }

    // Simpan notifikasi ke kotak per userId
    public void simpanNotifikasi(String userId, String pesan) {
        kotakNotifikasi.computeIfAbsent(userId, k -> new ArrayList<>()).add(pesan);
    }

    // Ambil semua notifikasi untuk userId (urutan terbaru di atas)
    public List<String> getNotifikasi(String userId) {
        List<String> list = kotakNotifikasi.getOrDefault(userId, new ArrayList<>());
        List<String> reversed = new ArrayList<>(list);
        Collections.reverse(reversed);
        return reversed;
    }
}