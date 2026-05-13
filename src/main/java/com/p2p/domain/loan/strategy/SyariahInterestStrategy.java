package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;

public class SyariahInterestStrategy extends BaseInterestStrategy {

    private final BigDecimal flatMargin;

    public SyariahInterestStrategy(BigDecimal flatMargin) {
        this.flatMargin = flatMargin;
    }

    /**
     * Tidak ada bunga berbasis persentase, tetapi hanya margin flat tetap yang ditambahkan ke pokok cicilan 
     * setiap bulan.
     */
    @Override
    public Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        BigDecimal principalPmt = calculatePrincipalPortion(initialPrincipal, tenor).getAmount();
        return new Money(principalPmt.add(flatMargin), initialPrincipal.getCurrency());
    }
}