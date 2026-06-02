package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class OverdueState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan == null) {
			throw new IllegalStateException("Loan tidak boleh null");
		}
		String status = loan.getStatus();
		// OVERDUE bisa terjadi dari DISBURSED (belum bayar sama sekali)
		// atau dari REPAYMENT (sudah pernah bayar tapi telat lagi)
		if ("DISBURSED".equals(status) || "REPAYMENT".equals(status)) {
			if (!loan.isPinjamanOverdue()) {
				throw new IllegalStateException(
					"Loan belum jatuh tempo, tidak bisa ditandai OVERDUE"
				);
			}
			loan.ubahStatus("OVERDUE");
		} else {
			throw new IllegalStateException(
				"Tidak bisa OVERDUE dari status: " + status
			);
		}
	}
}