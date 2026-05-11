package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedInterestStrategy implements InterestCalculationStrategy {
    private final BigDecimal rate;

    public FixedInterestStrategy(BigDecimal rate) {
        this.rate = rate;
    }

    @Override
    public Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        BigDecimal principalPmt = initialPrincipal.getAmount().divide(new BigDecimal(tenor), RoundingMode.HALF_UP);
        BigDecimal interestPmt = initialPrincipal.getAmount().multiply(rate);
        return new Money(principalPmt.add(interestPmt), "IDR");
    }
}
