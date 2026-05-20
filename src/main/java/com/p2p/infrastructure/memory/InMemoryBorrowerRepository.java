package com.p2p.infrastructure.memory;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryBorrowerRepository implements BorrowerRepository {
    private final Map<BorrowerId, Borrower> borrowers = new HashMap<>();

    @Override
    public Borrower findById(BorrowerId id) {
        return borrowers.get(id);
    }

    @Override
    public void save(Borrower borrower) {
        if (borrower != null && borrower.getId() != null) {
            borrowers.put(borrower.getId(), borrower);
        }
    }

    @Override
    public List<Borrower> findAll() {
        return new ArrayList<>(borrowers.values());
    }

    public void clear() {
        borrowers.clear();
    }
}
