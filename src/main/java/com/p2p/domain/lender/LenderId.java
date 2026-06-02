package com.p2p.domain.lender;

import java.util.Objects;

public class LenderId {
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();
    private final String value;

    public LenderId() {
        this.value = "LND-" + System.currentTimeMillis() + "-" + RANDOM.nextInt(100);
    }
    
    public LenderId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Lender ID tidak boleh kosong");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LenderId lenderId = (LenderId) o;
        return value.equals(lenderId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}