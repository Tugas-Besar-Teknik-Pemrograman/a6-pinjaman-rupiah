package com.p2p.bdd;

import com.p2p.application.service.UserService;
import com.p2p.domain.User.User;
import com.p2p.domain.User.UserRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegistrasiPenggunaSteps {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User hasilUser;
    private Exception exceptionDitolak;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        hasilUser = null;
        exceptionDitolak = null;
    }

    @Given("Sistem belum memiliki pengguna dengan email {string}")
    public void sistem_belum_memiliki_pengguna_dengan_email(String email) {
        when(userRepository.findByEmail(email)).thenReturn(null);
    }

    @Given("Pengguna dengan email {string} sudah terdaftar di sistem")
    public void pengguna_dengan_email_sudah_terdaftar_di_sistem(String email) {
        User existingUser = new User("User Lama", email, "password123", 25, 1);
        when(userRepository.findByEmail(email)).thenReturn(existingUser);
    }

    @When("Calon pengguna mendaftar dengan nama {string}, email {string}, password {string}, usia {int} tahun, dan role {int}")
    public void calon_pengguna_mendaftar_dengan_nama_email_password_usia_tahun_dan_role(String nama, String email, String password, Integer usia, Integer role) {
        try {
            hasilUser = UserService.registerUser(nama, email, password, usia, role);
        } catch (Exception e) {
            exceptionDitolak = e;
        }
    }

    @When("Calon pengguna mencoba mendaftar menggunakan email {string}")
    public void calon_pengguna_mencoba_mendaftar_menggunakan_email(String email) {
        try {
            hasilUser = userService.registerUser("Dummy Name", email, "DummyPass123", 25, 1);
        } catch (Exception e) {
            exceptionDitolak = e;
        }
    }

    @Then("Sistem berhasil membuat akun pengguna baru")
    public void sistem_berhasil_membuat_akun_pengguna_baru() {
        assertNull(exceptionDitolak, "Registrasi harusnya sukses, tidak boleh ada exception!");
        assertNotNull(hasilUser, "Objek User harus berhasil terbentuk!");

        verify(userRepository, times(1)).save(any(User.class));
    }
    @Then("Pengguna {string} terdaftar sebagai {string}")
    public void pengguna_terdaftar_sebagai(String emailExpected, String roleNameExpected) {
        assertEquals(emailExpected, hasilUser.getEmail(), "Email yang terdaftar harus sama");

        String actualRoleName = hasilUser.getRole() == 1 ? "Borrower" : "Lender";
        assertEquals(roleNameExpected, actualRoleName, "Role pengguna tidak sesuai");
    }

    @Then("Sistem akan menolak registrasi dengan pesan {string}")
    public void sistem_akan_menolak_registrasi_dengan_pesan(String pesanErrorExpected) {
        assertNotNull(exceptionDitolak, "Sistem harusnya menolak dan melempar Exception!");
        assertNull(hasilUser, "Objek User tidak boleh terbentuk jika registrasi gagal!");
        assertEquals(pesanErrorExpected, exceptionDitolak.getMessage(), "Pesan error tidak cocok!");

        verify(userRepository, never()).save(any());
    }
}
