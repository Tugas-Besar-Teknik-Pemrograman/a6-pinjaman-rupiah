package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class RepaymentState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && ("DISBURSED".equals(loan.getStatus())|| "OVERDUE".equals(loan.getStatus()))) {
			loan.ubahStatus("REPAYMENT");
		} else {
			throw new IllegalStateException(
				"Tidak bisa REPAYMENT dari status: " + (loan != null ? loan.getStatus() : "null")
			);
		}
	}
}