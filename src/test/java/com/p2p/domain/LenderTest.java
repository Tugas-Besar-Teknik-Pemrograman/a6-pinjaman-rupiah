package com.p2p.domain;

import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class LenderTest {
    private Lender lender;

    @BeforeEach
    void  inisialisasi() {
        lender = new Lender(new LenderId("LDR-001"), new Money(new BigDecimal("5000000"), "IDR"));
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

    @Test
    void tarikSaldo_Ditolak_NominalKurangDari100K() {
        // Arrange
        Money nominalTarik = new Money(new BigDecimal("50000"), "IDR"); // Hanya 50k, kurang dari 100k

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            lender.tarikSaldo(nominalTarik);
        });

        assertEquals("Nominal penarikan minimal harus 100000", exception.getMessage());
    }

    @Test
    void tarikSaldo_Ditolak_SaldoTidakCukup() {
        // Arrange
        Money nominalTarik = new Money(new BigDecimal("6000000"), "IDR"); // Minta 6 juta, tapi saldo cuma 5 juta

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            lender.tarikSaldo(nominalTarik);
        });

        assertEquals("Saldo tidak mencukupi untuk melakukan penarikan", exception.getMessage());
    }

    @Test
    void setKycStatus_SetToTrue_LenderVerified() {
        // Arrange
        Lender newLender = new Lender(new LenderId("LDR-002"), new Money(new BigDecimal("1000000"), "IDR"));

        // Assert awal: KYC belum verified
        assertFalse(newLender.isKycVerified());

        // Act
        newLender.setKycStatus(true);

        // Assert: KYC sudah verified
        assertTrue(newLender.isKycVerified());
    }

    @Test
    void getSaldoBalance_ReturnCorrectAmount() {
        // Arrange
        BigDecimal expectedAmount = new BigDecimal("5000000");

        // Act
        Money saldo = lender.getSaldoBalance();

        // Assert
        assertEquals(expectedAmount, saldo.getAmount());
        assertEquals("IDR", saldo.getCurrency());
    }

}
