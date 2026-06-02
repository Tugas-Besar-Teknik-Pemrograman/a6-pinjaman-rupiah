package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanStatus;

public class OverdueState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan == null) {
			throw new IllegalStateException("Loan tidak boleh null");
		}
		LoanStatus status = loan.getStatusEnum();
		// OVERDUE bisa terjadi dari DISBURSED (belum bayar sama sekali)
		// atau dari REPAYMENT (sudah pernah bayar tapi telat lagi)
		if (status == LoanStatus.DISBURSED || status == LoanStatus.REPAYMENT) {
			if (!loan.isPinjamanOverdue()) {
				throw new IllegalStateException(
					"Loan belum jatuh tempo, tidak bisa ditandai OVERDUE"
				);
			}
			loan.setStatusEnum(LoanStatus.OVERDUE);
		} else {
			throw new IllegalStateException(
				"Tidak bisa OVERDUE dari status: " + (status != null ? status.name() : "null")
			);
		}
	}
}