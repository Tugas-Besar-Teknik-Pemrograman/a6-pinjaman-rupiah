package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class OverdueState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && "REPAYMENT".equals(loan.getStatus())) {
			loan.ubahStatus("OVERDUE");
		}
	}
}