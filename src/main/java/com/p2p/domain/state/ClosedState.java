package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanStatus;

public class ClosedState implements State {

    @Override
    public void ubahStatus(Loan loan) {
        if (loan == null) {
            throw new IllegalStateException("Loan tidak boleh null");
        }
        LoanStatus status = loan.getStatusEnum();
        
        // Melunasi pinjaman dari REPAYMENT atau OVERDUE (ketika isLunas() true)
        if (status == LoanStatus.REPAYMENT || status == LoanStatus.OVERDUE || status == LoanStatus.DISBURSED) {
            loan.setStatusEnum(LoanStatus.CLOSED);
        } else {
            throw new IllegalStateException(
                "Tidak bisa CLOSED dari status: " + (status != null ? status.name() : "null")
            );
        }
    }
}
