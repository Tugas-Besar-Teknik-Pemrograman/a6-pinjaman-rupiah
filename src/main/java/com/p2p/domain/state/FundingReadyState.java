package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanStatus;

public class FundingReadyState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null && loan.getStatusEnum() == LoanStatus.FUNDING) {
			loan.setStatusEnum(LoanStatus.FUNDING_READY);
		} else {
			throw new IllegalStateException(
				"Tidak bisa FUNDING_READY dari status: " + (loan != null ? loan.getStatus() : "null")
			);
		}
	}
}