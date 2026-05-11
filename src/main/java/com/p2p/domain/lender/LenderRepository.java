package com.p2p.domain.lender;

public interface LenderRepository {
    Lender findById(String lenderId);
    void save(Lender lender);
}
