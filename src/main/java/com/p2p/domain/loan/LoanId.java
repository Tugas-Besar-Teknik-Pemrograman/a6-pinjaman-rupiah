package com.p2p.domain.loan;

import java.util.Objects;

public class LoanId {
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();
    private final String value;

    public LoanId() {
        this.value = "LN-" + System.currentTimeMillis() + "-" + RANDOM.nextInt(100);
    }
    
    public LoanId(String value) {
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanId loanId = (LoanId) o;
        return value.equals(loanId.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}