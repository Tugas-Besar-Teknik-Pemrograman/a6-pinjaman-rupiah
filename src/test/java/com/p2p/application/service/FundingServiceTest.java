package com.p2p.application.service;

import com.p2p.application.observer.LoanEventPublisher;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.loan.Loan;
import com.p2p.domain.loan.LoanId;
import com.p2p.domain.loan.LoanRepository;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FundingServiceTest {

    private static final LenderId LENDER_ID = new LenderId("LND-TEST-01");
    private static final LoanId LOAN_ID = new LoanId("LN-TEST-01");
    private static final BorrowerId BORROWER_ID = new BorrowerId("BR-TEST-01");

    private FundingService buatService(LoanRepository loanRepo, LenderRepository lenderRepo) {
        return new FundingService(loanRepo, lenderRepo, new LoanEventPublisher());
    }

    // =========================================================
    // A2: Saldo lender TIDAK boleh berkurang jika investasi gagal
    // =========================================================

    @Test
    void invest_gagal_melebihi_target_saldo_lender_tidak_berubah() {
        // Loan dengan target 1 juta, sudah terkumpul 800rb → sisa 200rb
        Loan loan = new Loan(LOAN_ID, BORROWER_ID, new Money(new BigDecimal("1000000"), "IDR"), 12);
        loan.ubahStatus("FUNDING");
        loan.tambahPendanaan(new LenderId("LND-OTHER"), new Money(new BigDecimal("800000"), "IDR"));

        // Lender punya saldo 5 juta
        Lender lender = new Lender(LENDER_ID, new Money(new BigDecimal("5000000"), "IDR"));

        LoanRepository loanRepo = mock(LoanRepository.class);
        LenderRepository lenderRepo = mock(LenderRepository.class);
        when(loanRepo.findById(LOAN_ID)).thenReturn(loan);
        when(lenderRepo.findById(LENDER_ID)).thenReturn(lender);

        FundingService service = buatService(loanRepo, lenderRepo);

        // Coba investasi 500rb — melebihi sisa 200rb
        assertThrows(IllegalArgumentException.class, () ->
            service.invest(LENDER_ID, LOAN_ID, new Money(new BigDecimal("500000"), "IDR"))
        );

        // Saldo lender HARUS tetap 5 juta (tidak boleh berkurang)
        assertEquals(0,
            new BigDecimal("5000000").compareTo(lender.getSaldoBalance().getAmount()),
            "Saldo lender tidak boleh berkurang ketika investasi gagal karena melebihi target"
        );
    }
}
