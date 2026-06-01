package com.p2p.application.service;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;

/**
 * Hasil dari proses bayar cicilan.
 * info tagihan, nominal bayar, kembalian, dan tenor sisa.
 */
public class BayarCicilanResult {
    private final Money tagihan;
    private final Money dibayar;
    private final Money kembalian;
    private final int tenorSisa;
    private final String statusPinjaman;

    public BayarCicilanResult(Money tagihan, Money dibayar, int tenorSisa, String statusPinjaman) {
        this.tagihan = tagihan;
        this.dibayar = dibayar;
        BigDecimal selisih = dibayar.getAmount().subtract(tagihan.getAmount());
        this.kembalian = new Money(selisih.max(BigDecimal.ZERO), Money.IDR);
        this.tenorSisa = tenorSisa;
        this.statusPinjaman = statusPinjaman;
    }

    public Money getTagihan()        { return tagihan; }
    public Money getDibayar()        { return dibayar; }
    public Money getKembalian()      { return kembalian; }
    public int getTenorSisa()        { return tenorSisa; }
    public String getStatusPinjaman(){ return statusPinjaman; }

    public boolean adaKembalian() {
        return kembalian.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }
}