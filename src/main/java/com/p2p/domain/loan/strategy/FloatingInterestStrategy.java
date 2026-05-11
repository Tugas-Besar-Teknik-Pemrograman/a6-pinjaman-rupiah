package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FloatingInterestStrategy implements InterestCalculationStrategy {
    private final BigDecimal monthlyRate;

    public FloatingInterestStrategy(BigDecimal monthlyRate) {
        this.monthlyRate = monthlyRate;
    }

    @Override
    public Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        BigDecimal principalPmt = initialPrincipal.getAmount().divide(new BigDecimal(tenor), RoundingMode.HALF_UP);
        BigDecimal interestPmt = remainingPrincipal.getAmount().multiply(monthlyRate);
        return new Money(principalPmt.add(interestPmt), "IDR");
    }
}