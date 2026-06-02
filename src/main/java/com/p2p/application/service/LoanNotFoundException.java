package com.p2p.application.service;

public class LoanNotFoundException extends Exception {
    public LoanNotFoundException(String message) {
        super(message);
    }
}
