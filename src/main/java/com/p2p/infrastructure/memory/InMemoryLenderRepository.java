package com.p2p.infrastructure.memory;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryLenderRepository implements LenderRepository {
    private final Map<LenderId, Lender> lenders = new HashMap<>();

    @Override
    public Lender findById(LenderId lenderId) {
        return lenders.get(lenderId);
    }

    @Override
    public void save(Lender lender) {
        if (lender != null && lender.getId() != null) {
            lenders.put(lender.getId(), lender);
        }
    }

    public void clear() {
        lenders.clear();
    }
}
