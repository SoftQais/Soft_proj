package service;

import library0.Book;
import library0.Fine;
import library0.Loan;
import library0.repository.BookRepository;
import library0.repository.FineRepository;
import library0.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private TimeProvider timeProvider;

    @Captor
    private ArgumentCaptor<Loan> loanCaptor;

    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        borrowService = new BorrowService(bookRepository, loanRepository, fineRepository, timeProvider);
    }

    @Test
    void borrowBook_successWhenNoRestrictions() {
        String userId = "U1";
        String bookId = "B1";
        LocalDate today = LocalDate.of(2025, 1, 1);

        when(timeProvider.today()).thenReturn(today);
        when(loanRepository.findAll()).thenReturn(Collections.emptyList()); // for generateFines + generateNextLoanId
        when(fineRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(loanRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        Book book = new Book(bookId, "Title", "Author", "123", 2, 2);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = borrowService.borrowBook(userId, bookId);

        assertEquals(userId, result.getUserId());
        assertEquals(bookId, result.getBookId());
        assertEquals(today.plusDays(28), result.getDueDate());

        verify(bookRepository).save(any(Book.class));
        verify(loanRepository).save(loanCaptor.capture());

        Loan saved = loanCaptor.getValue();
        assertEquals("U1", saved.getUserId());
        assertEquals("B1", saved.getBookId());
    }

    @Test
    void borrowBook_failsWhenUserHasUnpaidFines() {
        String userId = "U1";
        String bookId = "B1";
        LocalDate today = LocalDate.of(2025, 1, 1);

        when(timeProvider.today()).thenReturn(today);
        when(loanRepository.findAll()).thenReturn(Collections.emptyList());

        Fine fine = new Fine("F1", userId, "L1", 10, 0);
        when(fineRepository.findByUserId(userId))
                .thenReturn(Collections.singletonList(fine));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> borrowService.borrowBook(userId, bookId)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("unpaid"));
    }

    @Test
    void borrowBook_failsWhenUserHasOverdueLoan() {
        String userId = "U1";
        String bookId = "B1";
        LocalDate today = LocalDate.of(2025, 1, 1);

        when(timeProvider.today()).thenReturn(today);
        when(loanRepository.findAll()).thenReturn(Collections.emptyList());
        when(fineRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        Loan overdue = new Loan("L1", userId, "B9",
                today.minusDays(40),
                today.minusDays(10),
                null);

        when(loanRepository.findByUserId(userId))
                .thenReturn(Collections.singletonList(overdue));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> borrowService.borrowBook(userId, bookId)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("overdue"));
    }

    @Test
    void borrowBook_failsWhenUserHasThreeActiveLoans() {
        String userId = "U1";
        String bookId = "B1";
        LocalDate today = LocalDate.of(2025, 1, 1);

        when(timeProvider.today()).thenReturn(today);
        when(loanRepository.findAll()).thenReturn(Collections.emptyList());
        when(fineRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        Loan l1 = new Loan("L1", userId, "B1",
                today.minusDays(5), today.plusDays(20), null);
        Loan l2 = new Loan("L2", userId, "B2",
                today.minusDays(6), today.plusDays(21), null);
        Loan l3 = new Loan("L3", userId, "B3",
                today.minusDays(7), today.plusDays(22), null);

        when(loanRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(l1, l2, l3));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> borrowService.borrowBook(userId, bookId)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("3 active"));
    }
}