package com.p2p.domain.borrower;

import java.util.List;

public interface BorrowerRepository {
    Borrower findById(BorrowerId id);
    void save(Borrower borrower);
    List<Borrower> findAll();
}