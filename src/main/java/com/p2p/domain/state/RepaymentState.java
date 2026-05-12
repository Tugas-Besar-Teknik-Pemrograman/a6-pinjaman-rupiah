package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class RepaymentState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && "DISBURSED".equals(loan.getStatus())) {
			loan.ubahStatus("REPAYMENT");
		}
	}
}