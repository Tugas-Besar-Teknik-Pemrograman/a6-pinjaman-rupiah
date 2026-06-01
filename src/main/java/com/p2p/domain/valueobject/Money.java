package com.p2p.domain.valueobject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {

    public static final String IDR = "IDR";

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : null;
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

    public boolean isGreaterThan(Money nominal) {
        validasiCurrency(nominal);
        return this.amount.compareTo(nominal.getAmount()) > 0;
    }

    public Money add(Money nominal) {
        validasiCurrency(nominal);
        BigDecimal sumAmount = this.amount.add(nominal.getAmount());
        return new Money(sumAmount, this.currency);
    }

    public Money multiply(BigDecimal factor) {
        BigDecimal productAmount = this.amount.multiply(factor);
        return new Money(productAmount, this.currency);
    }

    public Money divide(BigDecimal divisor, RoundingMode roundingMode) {
        BigDecimal quotientAmount = this.amount.divide(divisor, 4, roundingMode);
        return new Money(quotientAmount, this.currency);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) &&
               Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}