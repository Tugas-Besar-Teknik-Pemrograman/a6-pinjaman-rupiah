package com.p2p.domain.event;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;

public class PencairanGagalEvent {
    private final LoanId loanId;
    private final BorrowerId borrowerId;
    private final String reason;

    public PencairanGagalEvent(LoanId loanId, BorrowerId borrowerId, String reason) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.reason = reason;
    }

    public LoanId getLoanId() { return loanId; }
    public BorrowerId getBorrowerId() { return borrowerId; }
    public String getReason() { return reason; }
}