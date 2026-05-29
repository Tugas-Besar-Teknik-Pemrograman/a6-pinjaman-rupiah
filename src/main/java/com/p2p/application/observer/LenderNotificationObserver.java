package com.p2p.application.observer;

import com.p2p.domain.event.*;

public class LenderNotificationObserver implements LoanObserver {

    @Override
    public void onPencairanBerhasil(PencairanBerhasilEvent event) {
        // Lender tidak perlu notifikasi event pencairan borrower
    }

    @Override
    public void onPencairanGagal(PencairanGagalEvent event) {
        // Lender tidak perlu notifikasi event pencairan borrower
    }

    @Override
    public void onPengajuanDiterima(PengajuanDiterimaEvent event) {
        // Lender tidak perlu notifikasi pengajuan borrower
    }

    @Override
    public void onPendanaanTerpenuhi(PendanaanTerpenuhiEvent event) {
        System.out.println("Notifikasi ke Lender: Pendanaan untuk pinjaman " + event.getLoanId() + " telah terpenuhi!");
    }

    @Override
    public void onCicilanBerhasil(CicilanBerhasilEvent event) {
        System.out.println("Notifikasi ke Lender: Cicilan sebesar " + event.getAmount() + " untuk pinjaman " + event.getLoanId() + " berhasil dibayar!");
    }

    @Override
    public void onPinjamanLunas(PinjamanLunasEvent event) {
        System.out.println("Notifikasi ke Lender: Pinjaman " + event.getLoanId() + " telah lunas!");
    }

    @Override
    public void onPinjamanJatuhTempo(PinjamanJatuhTempoEvent event) {
        System.out.println("Notifikasi ke Lender: Pinjaman " + event.getLoanId() + " jatuh tempo. " + event.getReason());
    }

    @Override
    public void onInvestasiDiterima(InvestasiDiterimaEvent event) {
        System.out.println("Notifikasi ke Lender " + event.getLenderId() + ": Investasi untuk pinjaman " + event.getLoanId() + " telah diterima!");
    }

    @Override
    public void onRefundLender(RefundLenderEvent event) {
        System.out.println("Notifikasi ke Lender " + event.getLenderId() + ": Refund sebesar " + event.getAmount() + " untuk pinjaman " + event.getLoanId() + " telah diproses!");
    }
}
