package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class DisbursedState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && "FUNDING_READY".equals(loan.getStatus())) {
			loan.ubahStatus("DISBURSED");
		} else {
			throw new IllegalStateException(
				"Tidak bisa DISBURSED dari status: " + (loan != null ? loan.getStatus() : "null")
			);
		}
	}
}