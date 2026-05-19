package com.p2p.domain.event;

import com.p2p.domain.lender.LenderId;
import com.p2p.domain.loan.LoanId;

public class InvestasiDiterimaEvent {
    private final LoanId loanId;
    private final LenderId lenderId;

    public InvestasiDiterimaEvent(LoanId loanId, LenderId lenderId) {
        this.loanId = loanId;
        this.lenderId = lenderId;
    }

    public LoanId getLoanId() { return loanId; }
    public LenderId getLenderId() { return lenderId; }
}