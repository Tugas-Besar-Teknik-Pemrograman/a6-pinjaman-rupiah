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
}