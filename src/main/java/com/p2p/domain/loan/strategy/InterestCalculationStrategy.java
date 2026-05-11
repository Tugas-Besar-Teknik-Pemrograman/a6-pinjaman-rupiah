package com.p2p.domain.loan.strategy;
import com.p2p.domain.valueobject.Money;

public interface InterestCalculationStrategy {
    Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor);
}