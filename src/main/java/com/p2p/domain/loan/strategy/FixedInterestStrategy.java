package com.p2p.domain.loan.strategy;
import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedInterestStrategy implements InterestCalculationStrategy {
    public FixedInterestStrategy(BigDecimal rate) {
        
    }
    @Override
    public Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        
        return null;
    }
}