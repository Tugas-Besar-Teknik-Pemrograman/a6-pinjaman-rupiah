package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public class ClosedState implements State {

	@Override
	public void ubahStatus(Loan loan) {
		if (loan == null) return;
		String status = loan.getStatus();
		if ("PENDING".equals(status) || "FUNDING".equals(status)) {
			if ("FUNDING".equals(status) && !loan.isPinjamanExpired()) {
				throw new IllegalStateException(
					"Pinjaman belum kadaluarsa, tidak bisa dibatalkan"
				);
			}
			loan.ubahStatus("CANCELED");
		} else {
			throw new IllegalStateException(
				"Tidak bisa CANCELED dari status: " + status
			);
		}
	}
}