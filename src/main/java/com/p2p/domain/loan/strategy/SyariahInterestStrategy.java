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
        BigDecimal principal = initialPrincipal.getAmount();
        BigDecimal t = new BigDecimal(tenor);
        
        // Rumus: (Pokok / Tenor) + Margin Tetap
        BigDecimal principalPerMonth = principal.divide(t, 2, RoundingMode.HALF_UP);
        BigDecimal monthly = principalPerMonth.add(flatMargin);
        
        return new Money(monthly, initialPrincipal.getCurrency());
    }
}