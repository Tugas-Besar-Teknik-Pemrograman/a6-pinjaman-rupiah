package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class CanceledState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && ("PENDING".equals(loan.getStatus()) || "FUNDING".equals(loan.getStatus()))) {
			loan.ubahStatus("CANCELED");
		}
	}
}