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
        
        return null;
    }
}