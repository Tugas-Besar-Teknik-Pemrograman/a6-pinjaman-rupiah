package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanStatus;

public class CanceledState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan != null) {
			LoanStatus s = loan.getStatusEnum();
			if (s == LoanStatus.PENDING || s == LoanStatus.FUNDING) {
				loan.setStatusEnum(LoanStatus.CANCELED);
				return;
			}
		}
		throw new IllegalStateException(
			"Tidak bisa CANCELED dari status: " + (loan != null ? loan.getStatus() : "null")
		);
	}
}