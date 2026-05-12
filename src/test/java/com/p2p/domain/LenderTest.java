package com.p2p.domain;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class LenderTest {
    private Lender lender;

    @BeforeEach
    void  inisialisasi() {
        lender = new Lender("LDR-001", new Money(new BigDecimal("5000000"), "IDR"));
        lender.setKycStatus(true);
    }

    @Test
    void tarikSaldo_Sukses_SaldoTerkurang() {
        // Arrange
        Money nominalTarik = new Money(new BigDecimal("500000"), "IDR");
        BigDecimal saldoAwalExpected = new BigDecimal("5000000");
        BigDecimal sisaSaldoExpected = new BigDecimal("4500000");

        // Assert saldo awal
        assertEquals(saldoAwalExpected, lender.getSaldoBalance().getAmount());

        // Act
        assertDoesNotThrow(() -> lender.tarikSaldo(nominalTarik));

        // Assert saldo berkurang
        assertEquals(sisaSaldoExpected, lender.getSaldoBalance().getAmount());
    }

    @Test
    void tambahSaldo_Sukses_SaldoBertambah() {
        // Arrange
        Money nominalTambah = new Money(new BigDecimal("2000000"), "IDR");
        BigDecimal saldoAwalExpected = new BigDecimal("5000000");
        BigDecimal saldoBaruExpected = new BigDecimal("7000000");

        // Assert saldo awal
        assertEquals(saldoAwalExpected, lender.getSaldoBalance().getAmount());

        // Act
        assertDoesNotThrow(() -> lender.tambahSaldo(nominalTambah));

        // Assert saldo bertambah
        assertEquals(saldoBaruExpected, lender.getSaldoBalance().getAmount());
    }

    @Test
    void tarikSaldo_Ditolak_KYCFalse() {
        // Arrange
        lender.setKycStatus(false); // Set KYC to false untuk test rejection
        Money nominalTarik = new Money(new BigDecimal("500000"), "IDR");

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            lender.tarikSaldo(nominalTarik);
        });

        assertEquals("Lender tidak terverifikasi (KYC = false)", exception.getMessage());
    }

}
