package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;

public interface InterestCalculationStrategy {
    Money hitungCicilan(Money initialPrincipal, Money remainingPrincipal, int tenor);
    Money hitungBagianPokok(Money initialPrincipal, int tenor);
}