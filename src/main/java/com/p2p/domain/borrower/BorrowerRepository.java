package com.p2p.domain.borrower;

public interface BorrowerRepository {
    Borrower findById(String id);

    void save(Borrower borrower);
}