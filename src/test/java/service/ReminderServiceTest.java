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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private Observer observer;

    private ReminderService reminderService;

    @BeforeEach
    void setUp() {
        reminderService = new ReminderService(loanRepository, userRepository, timeProvider);
        reminderService.addObserver(observer);
    }

    @Test
    void sendOverdueReminders_sendsOneReminderForOneOverdueLoan() {
        LocalDate today = LocalDate.of(2025, 1, 1);
        when(timeProvider.today()).thenReturn(today);

        Loan overdue = new Loan("L1", "U1", "B1",
                today.minusDays(40),
                today.minusDays(10),
                null);

        Loan notOverdue = new Loan("L2", "U1", "B2",
                today.minusDays(5),
                today.plusDays(10),
                null);

        when(loanRepository.findAll()).thenReturn(Arrays.asList(overdue, notOverdue));

        User user = new User("U1", "User", "user@lib.com", Role.CUSTOMER, "pwd");
        when(userRepository.findById("U1")).thenReturn(Optional.of(user));

        Map<String, Integer> result = reminderService.sendOverdueReminders();

        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(1), result.get("U1"));

        verify(observer, times(1))
                .notify(eq(user), eq("You have 1 overdue book(s)."));
    }
}