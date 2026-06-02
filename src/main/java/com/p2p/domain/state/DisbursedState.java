package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanStatus;

public class DisbursedState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && loan.getStatusEnum() == LoanStatus.FUNDING_READY) {
			loan.setStatusEnum(LoanStatus.DISBURSED);
		} else {
			throw new IllegalStateException(
					"Tidak bisa DISBURSED dari status: " + (loan != null ? loan.getStatus() : "null"));
		}
	}
}