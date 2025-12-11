package service;

import library0.Role;
import library0.User;
import library0.repository.UserRepository;
import service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    }

    @Test
    void loginAdmin_successWhenAdminAndPasswordCorrect() {
        User admin = new User("U1", "Admin", "admin@lib.com", Role.ADMIN, "admin123");

        when(userRepository.findByEmail("admin@lib.com"))
                .thenReturn(Optional.of(admin));

        boolean result = authService.loginAdmin("admin@lib.com", "admin123");

        assertTrue(result);
        assertTrue(authService.isAdminLoggedIn());
    }

    @Test
    void loginAdmin_failsWhenUserIsNotAdmin() {
        User customer = new User("U2", "User", "user@lib.com", Role.CUSTOMER, "user123");

        when(userRepository.findByEmail("user@lib.com"))
                .thenReturn(Optional.of(customer));

        boolean result = authService.loginAdmin("user@lib.com", "user123");

        assertFalse(result);
        assertFalse(authService.isAdminLoggedIn());
    }

    @Test
    void loginAdmin_failsWhenPasswordIncorrect() {
        User admin = new User("U1", "Admin", "admin@lib.com", Role.ADMIN, "admin123");

        when(userRepository.findByEmail("admin@lib.com"))
                .thenReturn(Optional.of(admin));

        boolean result = authService.loginAdmin("admin@lib.com", "wrong");

        assertFalse(result);
        assertFalse(authService.isAdminLoggedIn());
    }

    @Test
    void logout_clearsAdminSession() {
        User admin = new User("U1", "Admin", "admin@lib.com", Role.ADMIN, "admin123");
        when(userRepository.findByEmail("admin@lib.com"))
                .thenReturn(Optional.of(admin));

        authService.loginAdmin("admin@lib.com", "admin123");
        assertTrue(authService.isAdminLoggedIn());

        authService.logout();
        assertFalse(authService.isAdminLoggedIn());
    }
}