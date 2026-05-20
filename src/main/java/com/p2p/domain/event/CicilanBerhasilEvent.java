package com.p2p.domain.event;

import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.valueobject.Money;

public class CicilanBerhasilEvent {
    private final LoanId loanId;
    private final BorrowerId borrowerId;
    private final Money amount;

    public CicilanBerhasilEvent(LoanId loanId, BorrowerId borrowerId, Money amount) {
        this.loanId = loanId;
        this.borrowerId = borrowerId;
        this.amount = amount;
    }

    public LoanId getLoanId() { return loanId; }
    public BorrowerId getBorrowerId() { return borrowerId; }
    public Money getAmount() { return amount; }
}