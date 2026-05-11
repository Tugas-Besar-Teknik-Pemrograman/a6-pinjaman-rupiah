package com.p2p.domain.borrower;

import java.util.Optional;

public interface BorrowerRepository {
    Borrower findById(String id);

    void save(Borrower borrower);
}