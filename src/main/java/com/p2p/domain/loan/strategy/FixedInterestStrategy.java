package com.p2p.domain.loan.strategy;
import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedInterestStrategy implements InterestCalculationStrategy {
    private final BigDecimal rate;
    public FixedInterestStrategy(BigDecimal rate) { this.rate = rate; }

    @Override
    public Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        BigDecimal totalInterest = initialPrincipal.getAmount().multiply(rate).multiply(new BigDecimal(tenor));
        BigDecimal totalDebt = initialPrincipal.getAmount().add(totalInterest);
        return new Money(totalDebt.divide(new BigDecimal(tenor), 2, RoundingMode.HALF_UP), "IDR");
    }
}