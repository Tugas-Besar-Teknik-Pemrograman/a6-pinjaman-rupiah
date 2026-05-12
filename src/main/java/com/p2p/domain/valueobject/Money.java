package com.p2p.domain.valueobject;
import java.math.BigDecimal;

public class Money {

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() { 
        return amount; 
    }

    public String getCurrency() { 
        return currency; 
    }

    public boolean isLessThan(Money nominal) {
        validasiCurrency(nominal);
        return this.amount.compareTo(nominal.getAmount()) < 0;
    }

    public Money subtract(Money nominal) {
        validasiCurrency(nominal);
        BigDecimal sisaAmount = this.amount.subtract(nominal.getAmount());
        return new Money(sisaAmount, this.currency);
    }

    private void validasiCurrency(Money nominal) {
        if (!this.currency.equals(nominal.getCurrency())) {
            throw new IllegalArgumentException("Mata uang ga cocok");
        }
    }
}