package com.p2p.domain.lender;

import java.util.List;

public interface LenderRepository {
    Lender findById(LenderId lenderId);
    void save(Lender lender);
    List<Lender> findAll();
}
