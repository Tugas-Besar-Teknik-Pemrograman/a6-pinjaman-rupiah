package com.p2p.application.observer;

import com.p2p.domain.event.*;

public class BorrowerNotificationObserver implements LoanObserver {

    @Override
    public void onPencairanBerhasil(PencairanBerhasilEvent event) {
        System.out.println("Notifikasi ke Borrower " + event.getBorrowerId() + ": Dana pinjaman " + event.getLoanId() + " telah berhasil dicairkan!");
    }

    @Override
    public void onPencairanGagal(PencairanGagalEvent event) {
        System.out.println("Notifikasi ke Borrower " + event.getBorrowerId() + ": Pencairan gagal. Alasan: " + event.getReason());
    }

    @Override
    public void onPengajuanDiterima(PengajuanDiterimaEvent event) {
        System.out.println("Notifikasi ke Borrower " + event.getBorrowerId() + ": Pengajuan pinjaman " + event.getLoanId() + " telah diterima!");
    }

    @Override
    public void onPendanaanTerpenuhi(PendanaanTerpenuhiEvent event) {
        System.out.println("Notifikasi ke Borrower " + event.getBorrowerId() + ": Pendanaan untuk pinjaman " + event.getLoanId() + " telah terpenuhi!");
    }

    @Override
    public void onCicilanBerhasil(CicilanBerhasilEvent event) {
        System.out.println("Notifikasi ke Borrower " + event.getBorrowerId() + ": Cicilan sebesar " + event.getAmount() + " berhasil dibayar!");
    }

    @Override
    public void onPinjamanLunas(PinjamanLunasEvent event) {
        System.out.println("Notifikasi ke Borrower " + event.getBorrowerId() + ": Pinjaman " + event.getLoanId() + " telah lunas!");
    }

    @Override
    public void onPinjamanJatuhTempo(PinjamanJatuhTempoEvent event) {
        System.out.println("Notifikasi ke Borrower " + event.getBorrowerId() + ": Pinjaman " + event.getLoanId() + " jatuh tempo. " + event.getReason());
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