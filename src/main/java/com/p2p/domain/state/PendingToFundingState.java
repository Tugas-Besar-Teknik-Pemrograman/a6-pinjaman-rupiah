package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class PendingToFundingState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && "PENDING".equals(loan.getStatus())) {
			loan.ubahStatus("FUNDING");
		}
	}
}