package com.p2p.application;

import com.p2p.application.service.UserService;
import com.p2p.domain.user.User;
import com.p2p.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User userDummy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDummy = new User("Faqih Shiam", "faqih@gmail.com", "passwordKuat123", 20, 1,new BigDecimal("1000000"));
    }

    @Test
    void login_Sukses_MengembalikanObjekUser() {
        when(userRepository.findByEmail("faqih@gmail.com")).thenReturn(userDummy);

        User result = userService.login("faqih@gmail.com", "passwordKuat123");

        assertNotNull(result, "User tidak boleh null jika login berhasil");
        assertEquals("faqih@gmail.com", result.getEmail(), "Email harus cocok");
        assertEquals("Faqih Shiam", result.getNama(), "Nama harus cocok");
    }

    @Test
    void login_Gagal_EmailTidakDitemukan_MelemparException() {
        when(userRepository.findByEmail("anonim@gmail.com")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login("anonim@gmail.com", "bebas123");
        });

        assertEquals("Email tidak ditemukan", exception.getMessage());
    }

    @Test
    void login_Gagal_PasswordSalah_MelemparException() {
        when(userRepository.findByEmail("faqih@gmail.com")).thenReturn(userDummy);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.login("faqih@gmail.com", "passwordSALAH");
        });

        assertEquals("Password salah", exception.getMessage());
    }
}