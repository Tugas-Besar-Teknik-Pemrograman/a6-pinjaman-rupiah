package com.p2p.infrastructure.memory;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryBorrowerRepository implements BorrowerRepository {
    private final Map<String, Borrower> borrowers = new HashMap<>();

    @Override
    public Borrower findById(String id) {
        return borrowers.get(id);
    }

    @Override
    public void save(Borrower borrower) {
        if (borrower != null && borrower.getId() != null) {
            borrowers.put(borrower.getId(), borrower);
        }
    }
}
