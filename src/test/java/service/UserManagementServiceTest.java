package service;

import library0.Loan;
import library0.Role;
import library0.User;
import library0.repository.LoanRepository;
import library0.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoanRepository loanRepository;

    private UserManagementService userManagementService;

    @BeforeEach
    void setUp() {
        userManagementService = new UserManagementService(userRepository, loanRepository);
    }

    @Test
    void registerUser_successWhenEmailNotUsed() {
        when(userRepository.findByEmail("new@lib.com"))
                .thenReturn(Optional.empty());

        User created = new User("U3", "New User", "new@lib.com", Role.CUSTOMER, "pwd");

        when(userRepository.save(any(User.class)))
                .thenReturn(created);

        User result = userManagementService.registerUser("U3", "New User", "new@lib.com", "pwd");

        assertEquals("U3", result.getId());
        assertEquals("New User", result.getName());
        assertEquals("new@lib.com", result.getEmail());
    }

    @Test
    void registerUser_failsWhenEmailAlreadyExists() {
        User existing = new User("U5", "Old", "old@lib.com", Role.CUSTOMER, "pwd");
        when(userRepository.findByEmail("old@lib.com"))
                .thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementService.registerUser("U6", "X", "old@lib.com", "pwd2")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void unregisterUser_returnsFalseWhenUserHasActiveLoan() {
        User user = new User("U1", "User", "user@lib.com", Role.CUSTOMER, "pwd");
        when(userRepository.findById("U1"))
                .thenReturn(Optional.of(user));

        LocalDate today = LocalDate.of(2025, 1, 1);
        Loan activeLoan = new Loan("L1", "U1", "B1",
                today.minusDays(5), today.plusDays(20), null);

        when(loanRepository.findByUserId("U1"))
                .thenReturn(Collections.singletonList(activeLoan));

        boolean result = userManagementService.unregisterUser("U1");

        assertFalse(result);
        verify(userRepository, never()).deleteById("U1");
    }

    @Test
    void unregisterUser_returnsTrueWhenAllLoansReturned() {
        User user = new User("U1", "User", "user@lib.com", Role.CUSTOMER, "pwd");
        when(userRepository.findById("U1"))
                .thenReturn(Optional.of(user));

        LocalDate today = LocalDate.of(2025, 1, 1);
        Loan returnedLoan = new Loan("L1", "U1", "B1",
                today.minusDays(30), today.minusDays(10), today.minusDays(5));

        when(loanRepository.findByUserId("U1"))
                .thenReturn(Collections.singletonList(returnedLoan));

        boolean result = userManagementService.unregisterUser("U1");

        assertTrue(result);
        verify(userRepository, times(1)).deleteById("U1");
    }
}