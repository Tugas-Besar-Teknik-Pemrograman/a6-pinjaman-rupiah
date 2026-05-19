package com.p2p.application.observer;

import com.p2p.domain.event.*;

public interface LoanObserver {
    void onPencairanBerhasil(PencairanBerhasilEvent event);
    void onPencairanGagal(PencairanGagalEvent event);
    void onPengajuanDiterima(PengajuanDiterimaEvent event);
    void onPendanaanTerpenuhi(PendanaanTerpenuhiEvent event);
    void onCicilanBerhasil(CicilanBerhasilEvent event);
    void onPinjamanLunas(PinjamanLunasEvent event);
    void onPinjamanJatuhTempo(PinjamanJatuhTempoEvent event);
    void onInvestasiDiterima(InvestasiDiterimaEvent event);
    void onRefundLender(RefundLenderEvent event);
}