package com.p2p.application.observer;

public class BorrowerNotificationObserver implements LoanObserver {
    @Override
    public void onLoanEvent(String eventType, String loanId, String borrowerId) {
        switch (eventType) {
            case "PENCAIRAN_BERHASIL":
                System.out.println("Notifikasi ke Borrower " + borrowerId + ": Pencairan berhasil untuk Loan " + loanId);
                break;
            case "PENCAIRAN_GAGAL":
                System.out.println("Notifikasi ke Borrower " + borrowerId + ": Pencairan gagal untuk Loan " + loanId);
                break;
            default:
                System.out.println("Notifikasi ke Borrower " + borrowerId + ": Event " + eventType + " terjadi untuk Loan " + loanId);
        }
    }
}