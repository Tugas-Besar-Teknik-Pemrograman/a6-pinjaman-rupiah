package com.p2p.application.service;

public class BorrowerNotFoundException extends RuntimeException {
    public BorrowerNotFoundException(String message) {
        super(message);
    }
}
