package com.p2p.domain;
public class Loan {
    // Enum untuk status loan
    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    private Status status;

    // Saat loan dibuat, status awal adalah PENDING
    public Loan() {
        this.status = Status.PENDING;
    }

    // Setter untuk mengubah status loan
    public void setStatus(Status status) {
        this.status = status;
    }

    // Getter untuk membaca status loan
    public Status getStatus() {
        return status;
    }

    public void approve() {
        this.status = Status.APPROVED;
    }

    public void reject() {
        this.status = Status.REJECTED;
    }
}