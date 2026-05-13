package com.p2p.domain.user;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class UserId {
    private final String value;

    public UserId(int role) {
        String roleCode = (role == 1) ? "BRW" : (role == 2) ? "LND" : "ADM";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String uniqueTail = String.format("%04d", (int)(Math.random() * 10000));

        this.value = "USR-" + roleCode + "-" + datePart + "-" + uniqueTail;
    }

    public UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID tidak boleh kosong");
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return value.equals(userId.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
