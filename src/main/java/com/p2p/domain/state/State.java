package com.p2p.domain.state;

import com.p2p.domain.loan.Loan;

public interface State {

	void ubahStatus(Loan loan);
}
