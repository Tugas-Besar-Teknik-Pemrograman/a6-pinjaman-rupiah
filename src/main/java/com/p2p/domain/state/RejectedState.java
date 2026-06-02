package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class RejectedState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && ("PENDING".equals(loan.getStatus()) || "FUNDING_READY".equals(loan.getStatus()))) {
			loan.ubahStatus("REJECTED");
		} else {
			throw new IllegalStateException(
				"Tidak bisa REJECTED dari status: " + (loan != null ? loan.getStatus() : "null")
			);
		}
	}
}