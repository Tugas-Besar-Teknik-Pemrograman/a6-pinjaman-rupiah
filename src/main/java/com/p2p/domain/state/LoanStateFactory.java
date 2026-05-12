package com.p2p.domain.state;

public final class LoanStateFactory {

	private LoanStateFactory() {
	}

	public static State pendingToFunding() {
		return new PendingToFundingState();
	}

	public static State fundingReady() {
		return new FundingReadyState();
	}

	public static State disbursed() {
		return new DisbursedState();
	}

	public static State repayment() {
		return new RepaymentState();
	}

	public static State closed() {
		return new ClosedState();
	}

	public static State cancelled() {
		return new CancelledState();
	}

	public static State overdue() {
		return new OverdueState();
	}

	public static State rejected() {
		return new RejectedState();
	}
}