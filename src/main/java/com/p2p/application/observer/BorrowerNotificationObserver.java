package com.p2p.application.observer;

import com.p2p.application.service.NotificationService;
import com.p2p.domain.event.*;

public class BorrowerNotificationObserver implements LoanObserver {

    private NotificationService notificationService;

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private void simpan(String userId, String pesan) {
        System.out.println(pesan);
        if (notificationService != null && userId != null) {
            notificationService.simpanNotifikasi(userId, pesan);
        }
    }

    @Override
    public void onPencairanBerhasil(PencairanBerhasilEvent event) {
        String pesan = "Dana pinjaman " + event.getLoanId() + " telah berhasil dicairkan!";
        simpan(event.getBorrowerId().getValue(), pesan);
    }

    @Override
    public void onPencairanGagal(PencairanGagalEvent event) {
        String pesan = "Pencairan gagal. Alasan: " + event.getReason();
        simpan(event.getBorrowerId().getValue(), pesan);
    }

    @Override
    public void onPengajuanDiterima(PengajuanDiterimaEvent event) {
        String pesan = "Pengajuan pinjaman " + event.getLoanId() + " telah diterima!";
        simpan(event.getBorrowerId().getValue(), pesan);
    }

    @Override
    public void onPendanaanTerpenuhi(PendanaanTerpenuhiEvent event) {
        String pesan = "Pendanaan untuk pinjaman " + event.getLoanId() + " telah terpenuhi 100%!";
        simpan(event.getBorrowerId().getValue(), pesan);
    }

    @Override
    public void onCicilanBerhasil(CicilanBerhasilEvent event) {
        String pesan = "Cicilan sebesar " + event.getAmount() + " berhasil dibayar!";
        simpan(event.getBorrowerId().getValue(), pesan);
    }

    @Override
    public void onPinjamanLunas(PinjamanLunasEvent event) {
        String pesan = "Pinjaman " + event.getLoanId() + " telah lunas!";
        simpan(event.getBorrowerId().getValue(), pesan);
    }

    @Override
    public void onPinjamanJatuhTempo(PinjamanJatuhTempoEvent event) {
        String pesan = "Pinjaman " + event.getLoanId() + " jatuh tempo. " + event.getReason();
        simpan(event.getBorrowerId().getValue(), pesan);
    }

    @Override
    public void onInvestasiDiterima(InvestasiDiterimaEvent event) {
        // Borrower tidak perlu notifikasi event investasi
    }

    @Override
    public void onRefundLender(RefundLenderEvent event) {
        // Borrower tidak perlu notifikasi event refund lender
    }
}