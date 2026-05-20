package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class ClosedState implements State {

    @Override
    public void ubahStatus(Loan loan) {
        if (loan == null) return;
        String status = loan.getStatus();
        
        // Melunasi pinjaman dari REPAYMENT atau OVERDUE (ketika isLunas() true)
        if ("REPAYMENT".equals(status) || "OVERDUE".equals(status) || "DISBURSED".equals(status)) {
            loan.ubahStatus("CLOSED");
        } else {
            throw new IllegalStateException(
                "Tidak bisa CLOSED dari status: " + status
            );
        }
    }
}
