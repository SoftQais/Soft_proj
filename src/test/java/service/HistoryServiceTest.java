package service;

import library0.Loan;
import library0.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private TimeProvider timeProvider; // موجود بس بدون when()

    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(loanRepository, timeProvider);
    }

    @Test
    void getUserHistory_returnsLoansForUser() {
        LocalDate today = LocalDate.of(2025, 1, 1);

        Loan l1 = new Loan("L1", "U1", "B1",
                today.minusDays(10), today.plusDays(5), null);
        Loan l2 = new Loan("L2", "U1", "B2",
                today.minusDays(20), today.minusDays(1), today.minusDays(1));

        when(loanRepository.findByUserId("U1"))
                .thenReturn(Arrays.asList(l1, l2));

        List<Loan> result = historyService.getUserHistory("U1");

        assertEquals(2, result.size());
        assertEquals("L1", result.get(0).getId());
        assertEquals("L2", result.get(1).getId());
    }
}