package com.p2p.domain.lender;

public interface LenderRepository {
    Lender findById(LenderId lenderId);
    void save(Lender lender);
}
