package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;

public class FixedInterestStrategy extends BaseInterestStrategy {

    private final BigDecimal rate;

    public FixedInterestStrategy(BigDecimal rate) {
        this.rate = rate;
    }

    /**
     * Bunga dihitung dari pokok awal pinjaman, bukan sisa pokok. Jadi cicilan tiap bulan selalu sama
     */
    @Override
    public Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        BigDecimal principalPmt = calculatePrincipalPortion(initialPrincipal, tenor).getAmount();
        BigDecimal interestPmt = initialPrincipal.getAmount().multiply(rate);
        return new Money(principalPmt.add(interestPmt), initialPrincipal.getCurrency());
    }
}