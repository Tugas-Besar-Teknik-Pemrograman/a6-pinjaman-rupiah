package com.p2p.domain.event;

public class PencairanBerhasilEvent {
    private String loanId;
    private String borrowerId;

    public PencairanBerhasilEvent(String loanId, String borrowerId) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
    }

    public String getLoanId() {
        return loanId;
    }

    public String getBorrowerId() {
        return borrowerId;
    }
}