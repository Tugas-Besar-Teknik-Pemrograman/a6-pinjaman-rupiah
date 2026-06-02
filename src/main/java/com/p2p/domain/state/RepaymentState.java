package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanStatus;

public class RepaymentState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null) {
			LoanStatus s = loan.getStatusEnum();
			if (s == LoanStatus.DISBURSED || s == LoanStatus.OVERDUE) {
				loan.setStatusEnum(LoanStatus.REPAYMENT);
				return;
			}
		}
		throw new IllegalStateException(
			"Tidak bisa REPAYMENT dari status: " + (loan != null ? loan.getStatus() : "null")
		);
	}
}