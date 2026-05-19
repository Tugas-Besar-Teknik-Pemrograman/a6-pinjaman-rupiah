package com.p2p.bdd;

import com.p2p.application.service.UserService;
import com.p2p.domain.user.User;
import com.p2p.domain.user.UserRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginSteps {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User loggedInUser;
    private Exception loginException;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loggedInUser = null;
        loginException = null;
    }

    @Given("Pengguna dengan email {string} dan password {string} sudah terdaftar sebagai role {int}")
    public void pengguna_sudah_terdaftar(String email, String password, Integer role) {
        // Buat mock user. ID akan di-generate otomatis oleh constructor User
        User mockUser = new User("User Testing", email, password, 20, role);
        when(userRepository.findByEmail(email)).thenReturn(mockUser);
    }

    @Given("Sistem belum memiliki pengguna dengan email {string} untuk login")
    public void sistem_belum_memiliki_pengguna_untuk_login(String email) {
        when(userRepository.findByEmail(email)).thenReturn(null);
    }

    @When("Pengguna mencoba login dengan email {string} dan password {string}")
    public void pengguna_mencoba_login(String email, String password) {
        try {
            // Memanggil method login di UserService
            loggedInUser = userService.login(email, password);
        } catch (Exception e) {
            loginException = e;
        }
    }

    @Then("Sistem akan mengizinkan akses")
    public void sistem_akan_mengizinkan_akses() {
        assertNull(loginException, "Seharusnya login berhasil tanpa melempar error");
        assertNotNull(loggedInUser, "Objek User harus dikembalikan setelah login sukses");
    }

    @Then("Sistem mengenali pengguna sebagai {string}")
    public void sistem_mengenali_pengguna_sebagai(String expectedRoleName) {
        String actualRoleName = loggedInUser.getRole() == 1 ? "Borrower" : "Lender";
        assertEquals(expectedRoleName, actualRoleName, "Role pengguna tidak sesuai");
    }

    @Then("Sistem akan menolak login dengan pesan {string}")
    public void sistem_akan_menolak_login_dengan_pesan(String expectedMessage) {
        assertNotNull(loginException, "Sistem seharusnya melempar exception");
        assertNull(loggedInUser, "User tidak boleh di-return jika login gagal");
        assertEquals(expectedMessage, loginException.getMessage(), "Pesan error tidak cocok");
    }
}