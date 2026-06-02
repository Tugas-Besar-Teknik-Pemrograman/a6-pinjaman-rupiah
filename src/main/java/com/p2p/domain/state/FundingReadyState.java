package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class FundingReadyState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && "FUNDING".equals(loan.getStatus())) {
			loan.ubahStatus("FUNDING_READY");
		} else {
			throw new IllegalStateException(
				"Tidak bisa FUNDING_READY dari status: " + (loan != null ? loan.getStatus() : "null")
			);
		}
	}
}