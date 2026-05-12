package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class DisbursedState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && "FUNDING_READY".equals(loan.getStatus())) {
			loan.ubahStatus("DISBURSED");
		}
	}
}