package com.p2p.domain.loan.strategy;

import com.p2p.domain.valueobject.Money;
import java.math.BigDecimal;

public class FloatingInterestStrategy extends BaseInterestStrategy {

    private final BigDecimal monthlyRate;

    public FloatingInterestStrategy(BigDecimal monthlyRate) {
        this.monthlyRate = monthlyRate;
    }

    /**
     * Bunga dihitung dari sisa pokok pinjaman, sehingga cicilan makin kecil setiap bulan.
     */
    @Override
    public Money calculateInstallment(Money initialPrincipal, Money remainingPrincipal, int tenor) {
        BigDecimal principalPmt = calculatePrincipalPortion(initialPrincipal, tenor).getAmount();
        BigDecimal interestPmt = remainingPrincipal.getAmount().multiply(monthlyRate);
        return new Money(principalPmt.add(interestPmt), initialPrincipal.getCurrency());
    }
}
    
