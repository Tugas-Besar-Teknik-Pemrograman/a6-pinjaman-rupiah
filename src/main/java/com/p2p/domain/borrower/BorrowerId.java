package com.p2p.domain.borrower;

import java.util.Objects;

public class BorrowerId {
    private final String value;

    public BorrowerId() {
        this.value = "BR-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 100);
    }

    public BorrowerId(String value) {
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowerId loanId = (BorrowerId) o;
        return value.equals(loanId.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
