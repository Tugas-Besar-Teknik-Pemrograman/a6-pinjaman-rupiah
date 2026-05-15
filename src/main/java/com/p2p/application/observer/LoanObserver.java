package com.p2p.application.observer;

public interface LoanObserver {
    void onLoanEvent(String eventType, String loanId, String borrowerId);
}