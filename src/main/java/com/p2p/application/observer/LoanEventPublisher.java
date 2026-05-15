package com.p2p.application.observer;

import java.util.ArrayList;
import java.util.List;

public class LoanEventPublisher {
    private final List<LoanObserver> observers = new ArrayList<>();

    public void registerObserver(LoanObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(LoanObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String eventType, String loanId, String borrowerId) {
        for (LoanObserver observer : observers) {
            observer.onLoanEvent(eventType, loanId, borrowerId);
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}