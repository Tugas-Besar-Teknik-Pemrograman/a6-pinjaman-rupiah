package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class BaseInterestStrategy implements InterestCalculationStrategy {

    @Override
    public Money calculatePrincipalPortion(Money initialPrincipal, int tenor) {
        BigDecimal principal = initialPrincipal.getAmount()
                .divide(BigDecimal.valueOf(tenor), RoundingMode.HALF_UP);
        return new Money(principal, initialPrincipal.getCurrency());
    }
}