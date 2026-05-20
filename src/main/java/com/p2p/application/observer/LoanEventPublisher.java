package com.p2p.application.observer;

import com.p2p.domain.event.*;
import java.util.ArrayList;
import java.util.List;

public class LoanEventPublisher {
    private final List<LoanObserver> observers = new ArrayList<>();

    public void registerObserver(LoanObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(LoanObserver observer) {
        observers.remove(observer);
    }

    public void publishPencairanBerhasil(PencairanBerhasilEvent event) {
        for (LoanObserver observer : observers) observer.onPencairanBerhasil(event);
    }

    public void publishPencairanGagal(PencairanGagalEvent event) {
        for (LoanObserver observer : observers) observer.onPencairanGagal(event);
    }

    public void publishPengajuanDiterima(PengajuanDiterimaEvent event) {
        for (LoanObserver observer : observers) observer.onPengajuanDiterima(event);
    }

    public void publishPendanaanTerpenuhi(PendanaanTerpenuhiEvent event) {
        for (LoanObserver observer : observers) observer.onPendanaanTerpenuhi(event);
    }

    public void publishCicilanBerhasil(CicilanBerhasilEvent event) {
        for (LoanObserver observer : observers) observer.onCicilanBerhasil(event);
    }

    public void publishPinjamanLunas(PinjamanLunasEvent event) {
        for (LoanObserver observer : observers) observer.onPinjamanLunas(event);
    }

    public void publishPinjamanJatuhTempo(PinjamanJatuhTempoEvent event) {
        for (LoanObserver observer : observers) observer.onPinjamanJatuhTempo(event);
    }

    public void publishInvestasiDiterima(InvestasiDiterimaEvent event) {
        for (LoanObserver observer : observers) observer.onInvestasiDiterima(event);
    }

    public void publishRefundLender(RefundLenderEvent event) {
        for (LoanObserver observer : observers) observer.onRefundLender(event);
    }

    public int getObserverCount() {
        return observers.size();
    }
}