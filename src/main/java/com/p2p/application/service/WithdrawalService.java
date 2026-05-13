package com.p2p.application.service;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.valueobject.Money;

public class WithdrawalService {

    private final LenderRepository lenderRepository;

    public WithdrawalService(LenderRepository lenderRepository) {
        this.lenderRepository = lenderRepository;
    }

    public void withdraw(LenderId lenderId, Money amount) {
        Lender lender = lenderRepository.findById(lenderId);
        if (lender == null) {
            throw new IllegalArgumentException("Lender tidak ditemukan");
        }

        lender.tarikSaldo(amount);

        lenderRepository.save(lender);
    }
}
