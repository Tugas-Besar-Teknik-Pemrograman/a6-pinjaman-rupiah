package com.p2p.domain.event;

import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.valueobject.Money;

public class RefundLenderEvent {
    private final LoanId loanId;
    private final LenderId lenderId;
    private final Money amount;

    public RefundLenderEvent(LoanId loanId, LenderId lenderId, Money amount) {
        this.loanId = loanId;
        this.lenderId = lenderId;
        this.amount = amount;
    }

    public LoanId getLoanId() { return loanId; }
    public LenderId getLenderId() { return lenderId; }
    public Money getAmount() { return amount; }
}