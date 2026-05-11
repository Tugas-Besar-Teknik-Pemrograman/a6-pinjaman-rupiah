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
        BigDecimal pInitial = initialPrincipal.getAmount();
        BigDecimal pRemaining = remainingPrincipal.getAmount();
        BigDecimal t = new BigDecimal(tenor);
        
        //Pokok Awal dibagi tenor untuk mendapatkan pokok per bulan
        BigDecimal principalInstallment = pInitial.divide(t, 2, RoundingMode.HALF_UP);
        
        //Sisa pokok dikali rate untuk mendapatkan bunga bulan ini
        BigDecimal interestThisMonth = pRemaining.multiply(monthlyRate);
        
        return new Money(principalInstallment.add(interestThisMonth), initialPrincipal.getCurrency());
    }
}