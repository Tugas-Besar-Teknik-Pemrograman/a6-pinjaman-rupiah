package com.p2p.service;

import com.p2p.domain.Borrower;
import com.p2p.domain.Loan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;

public class LoanService {

    // Inisialisasi logger
    private static final Logger logger = LogManager.getLogger(LoanService.class);

    public Loan createLoan(Borrower borrower, BigDecimal amount) {
        logger.info("Memulai proses pengajuan loan...");

        validateBorrower(borrower);
        validateAmount(amount);

        Loan loan = new Loan();

        if (borrower.getCreditScore() >= 600) {
            loan.approve();
            logger.info("Loan APPROVED - credit score: {}", borrower.getCreditScore());
        } else {
            loan.reject();
            logger.warn("Loan REJECTED - credit score terlalu rendah: {}", borrower.getCreditScore());
        }

        logger.info("Proses loan selesai dengan status: {}", loan.getStatus());
        return loan;
    }

    private void validateBorrower(Borrower borrower) {
        logger.debug("Validasi borrower - verified: {}, credit score: {}",
                borrower.isVerified(), borrower.getCreditScore());

        if (!borrower.canApplyLoan()) {
            logger.error("Borrower tidak terverifikasi! Pengajuan ditolak.");
            throw new IllegalArgumentException("Borrower not verified");
        }

        logger.debug("Validasi borrower berhasil.");
    }

    private void validateAmount(BigDecimal amount) {
        logger.debug("Validasi amount: {}", amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Amount tidak valid: {}. Harus lebih dari 0.", amount);
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        logger.debug("Validasi amount berhasil.");
    }
}