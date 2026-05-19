package com.p2p.domain;

import com.p2p.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("[UT-01] Berhasil membuat instansiasi User Borrower jika data valid")
    void testCreateUserBorrowerValid() {
        User user = new User("Faqih1", "Faqihborrower@gmail.com", "borrower1234", 20, 1);

        assertNotNull(user);
        assertEquals("Faqih1", user.getNama());
        assertEquals("Faqihborrower@gmail.com", user.getEmail());
        assertEquals("borrower1234", user.getPassword());
        assertEquals(20, user.getUsia());
        assertEquals(1, user.getRole());
    }

    @Test
    @DisplayName("[UT-02] Berhasil membuat User Lender dengan role 2")
    void testCreateUserLenderValid() {
        User user = new User("Faqih2", "Faqihlender@gmail.com", "Lender123", 25, 2);

        assertNotNull(user);
        assertEquals("Faqih2", user.getNama());
        assertEquals("Faqihlender@gmail.com", user.getEmail());
        assertEquals(25, user.getUsia());
        assertEquals(2, user.getRole());
    }

    @Test
    @DisplayName("[UT-03] UserId ter-generate secara otomatis dan tidak null")
    void testUserIdGeneratedNotNull() {
        User user = new User("Faqih1", "Faqihborrower@gmail.com", "borrower1234", 20, 1);

        assertNotNull(user.getId(), "UserId tidak boleh null setelah objek User dibuat");
        assertNotNull(user.getId().getValue(), "Nilai string dari UserId tidak boleh null");
    }

    @Test
    @DisplayName("[UT-04] UserId Borrower mengandung prefix yang benar (USR-BRW)")
    void testUserIdBorrowerFormat() {
        User user = new User("Faqih1", "Faqihborrower@gmail.com", "borrower1234", 20, 1);
        String idValue = user.getId().getValue();

        assertTrue(idValue.startsWith("USR-BRW-"),
                "UserId Borrower harus diawali dengan 'USR-BRW-', tapi dapat: " + idValue);
    }

    @Test
    @DisplayName("[UT-05] UserId Lender mengandung prefix yang benar (USR-LND)")
    void testUserIdLenderFormat() {
        User user = new User("Faqih2", "Faqihlender@gmail.com", "Lender123", 25, 2);
        String idValue = user.getId().getValue();

        assertTrue(idValue.startsWith("USR-LND-"),
                "UserId Lender harus diawali dengan 'USR-LND-', tapi dapat: " + idValue);
    }

    @Test
    @DisplayName("[UT-06] Berhasil membuat User pada batas usia minimum (tepat 18 tahun)")
    void testCreateUserBoundaryAgeExact18() {
        // Tepat di batas: harus BERHASIL
        User user = new User("BatasUsia", "batas@gmail.com", "Pass1234", 18, 1);

        assertNotNull(user);
        assertEquals(18, user.getUsia());
    }

    @Test
    @DisplayName("[UT-07] Gagal membuat User jika usia tepat 1 tahun di bawah batas (17 tahun)")
    void testCreateUserAgeJustBelow18() {
        // Tepat 1 di bawah batas: harus GAGAL
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new User("BawahUsia", "bawah@gmail.com", "Pass1234", 17, 1)
        );

        assertEquals("Usia minimal untuk mendaftar adalah 18 tahun", exception.getMessage());
    }

    @Test
    @DisplayName("[UT-08] Gagal membuat User jika usia di bawah 18 tahun (16 tahun)")
    void testCreateUserInvalidAge() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new User("Faqih", "Faqihborrower@gmail.com", "Faqihborrower123", 16, 1)
        );

        assertEquals("Usia minimal untuk mendaftar adalah 18 tahun", exception.getMessage());
    }

    @Test
    @DisplayName("[UT-09] Gagal membuat User jika pilihan role tidak valid (contoh: 4)")
    void testCreateUserInvalidRole() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new User("Faqih3", "faqihinvalid@gmail.com", "Pass123", 20, 4)
        );

        assertEquals("Role pengguna tidak valid", exception.getMessage());
    }

    @Test
    @DisplayName("[UT-10] Gagal membuat User jika role 0 (tidak ada role 0)")
    void testCreateUserRoleZeroInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new User("Faqih4", "faqihzero@gmail.com", "Pass123", 22, 0)
        );

        assertEquals("Role pengguna tidak valid", exception.getMessage());
    }
}
