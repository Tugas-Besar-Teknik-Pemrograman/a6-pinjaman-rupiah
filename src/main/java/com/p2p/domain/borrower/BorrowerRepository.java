package com.p2p.domain.borrower;

public interface BorrowerRepository {
    Borrower findById(BorrowerId id);

    void save(Borrower borrower);
}