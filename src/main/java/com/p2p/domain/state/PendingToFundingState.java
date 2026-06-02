package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanStatus;

public class PendingToFundingState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && loan.getStatusEnum() == LoanStatus.PENDING) {
			loan.setStatusEnum(LoanStatus.FUNDING);
		} else {
			throw new IllegalStateException(
				"Tidak bisa FUNDING dari status: " + (loan != null ? loan.getStatus() : "null")
			);
		}
	}
}