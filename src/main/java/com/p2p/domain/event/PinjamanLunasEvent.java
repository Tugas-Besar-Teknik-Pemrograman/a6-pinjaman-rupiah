package com.p2p.domain.event;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;

public class PinjamanLunasEvent {
    private final LoanId loanId;
    private final BorrowerId borrowerId;

    public PinjamanLunasEvent(LoanId loanId, BorrowerId borrowerId) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
    }

    public LoanId getLoanId() { return loanId; }
    public BorrowerId getBorrowerId() { return borrowerId; }
}