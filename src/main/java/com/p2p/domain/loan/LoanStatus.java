package com.p2p.domain.loan;

public enum LoanStatus {
    PENDING,
    FUNDING,
    FUNDING_READY,
    DISBURSED,
    REPAYMENT,
    OVERDUE,
    CLOSED,
    REJECTED,
    CANCELED;

    public static LoanStatus fromString(String s) {
        if (s == null) return null;
        String norm = s.trim().toUpperCase();
        if ("CANCELLED".equals(norm) || "CANCELED".equals(norm)) return CANCELED;
        return LoanStatus.valueOf(norm);
    }

    @Override
    public String toString() {
        return name();
    }
}
