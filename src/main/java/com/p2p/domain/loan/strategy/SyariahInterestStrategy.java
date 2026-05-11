package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SyariahInterestStrategy implements InterestCalculationStrategy {
    private final BigDecimal flatMargin;

    public SyariahInterestStrategy(BigDecimal flatMargin) {
        this.flatMargin = flatMargin;
    }

    @Override
    public Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        BigDecimal principalPmt = initialPrincipal.getAmount().divide(new BigDecimal(tenor), RoundingMode.HALF_UP);
        return new Money(principalPmt.add(flatMargin), "IDR");
    }
}