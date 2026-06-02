package com.p2p.application.observer;

import com.p2p.application.service.NotificationService;
import com.p2p.domain.event.*;

public class LenderNotificationObserver implements LoanObserver {

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
        // Lender tidak perlu notifikasi pencairan borrower
    }

    @Override
    public void onPencairanGagal(PencairanGagalEvent event) {
        // Lender tidak perlu notifikasi pencairan borrower
    }

    @Override
    public void onPengajuanDiterima(PengajuanDiterimaEvent event) {
        // Lender tidak perlu notifikasi pengajuan borrower
    }

    @Override
    public void onPendanaanTerpenuhi(PendanaanTerpenuhiEvent event) {
        String pesan = "Pendanaan untuk pinjaman " + event.getLoanId() + " telah terpenuhi 100%!";
        simpan(null, pesan); // broadcast, tidak per-lender
    }

    @Override
    public void onCicilanBerhasil(CicilanBerhasilEvent event) {
        String pesan = "Cicilan sebesar " + event.getAmount() + " untuk pinjaman " + event.getLoanId() + " berhasil dibayar!";
        simpan(null, pesan); // broadcast, tidak per-lender
    }

    @Override
    public void onPinjamanLunas(PinjamanLunasEvent event) {
        String pesan = "Pinjaman " + event.getLoanId() + " telah lunas!";
        simpan(null, pesan);
    }

    @Override
    public void onPinjamanJatuhTempo(PinjamanJatuhTempoEvent event) {
        String pesan = "Pinjaman " + event.getLoanId() + " jatuh tempo. " + event.getReason();
        simpan(null, pesan);
    }

    @Override
    public void onInvestasiDiterima(InvestasiDiterimaEvent event) {
        String pesan = "Investasi untuk pinjaman " + event.getLoanId() + " telah diterima!";
        simpan(event.getLenderId().getValue(), pesan); // per-lender ✅
    }

    @Override
    public void onRefundLender(RefundLenderEvent event) {
        String pesan = "Refund sebesar " + event.getAmount() + " untuk pinjaman " + event.getLoanId() + " telah diproses!";
        simpan(event.getLenderId().getValue(), pesan); // per-lender ✅
    }
}