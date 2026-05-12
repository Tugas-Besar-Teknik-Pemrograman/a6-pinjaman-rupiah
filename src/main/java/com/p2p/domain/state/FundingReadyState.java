package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class FundingReadyState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && "FUNDING".equals(loan.getStatus())) {
			loan.ubahStatus("FUNDING_READY");
		}
	}
}