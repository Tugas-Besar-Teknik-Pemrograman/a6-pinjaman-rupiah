package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;

public class FixedInterestStrategy extends BaseInterestStrategy {

    private final BigDecimal rate;

    public FixedInterestStrategy(BigDecimal rate) {
        this.rate = rate;
    }

    /**
     * Bunga FLAT: dihitung dari pokok AWAL (bukan sisa pokok).
     * Jadi bunga tiap bulan selalu sama meski sisa pokok berkurang.
     * Contoh: pokok 1jt, rate 5%, bunga tiap bulan = 1jt * 5% = 50rb (tetap)
     */
    @Override
    public Money hitungCicilan(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        BigDecimal principalPmt = hitungBagianPokok(initialPrincipal, tenor).getAmount();
        // Bunga flat: dari pokok AWAL, bukan sisa pokok
        BigDecimal interestPmt = initialPrincipal.getAmount().multiply(rate);
        return new Money(principalPmt.add(interestPmt), initialPrincipal.getCurrency());
    }
}