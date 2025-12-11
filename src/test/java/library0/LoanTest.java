package library0;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {

    @Test
    void isReturned_trueWhenReturnedDateNotNull() {
        LocalDate today = LocalDate.of(2025, 1, 1);
        Loan loan = new Loan("L1", "U1", "B1",
                today.minusDays(10), today.plusDays(10), today);

        assertTrue(loan.isReturned());
    }

    @Test
    void isReturned_falseWhenReturnedDateNull() {
        LocalDate today = LocalDate.of(2025, 1, 1);
        Loan loan = new Loan("L1", "U1", "B1",
                today.minusDays(10), today.plusDays(10), null);

        assertFalse(loan.isReturned());
    }

    @Test
    void isOverdue_trueWhenPastDueAndNotReturned() {
        LocalDate today = LocalDate.of(2025, 1, 10);
        Loan loan = new Loan("L1", "U1", "B1",
                today.minusDays(40), today.minusDays(5), null);

        assertTrue(loan.isOverdue(today));
    }

    @Test
    void isOverdue_falseWhenBeforeDueDate() {
        LocalDate today = LocalDate.of(2025, 1, 10);
        Loan loan = new Loan("L1", "U1", "B1",
                today.minusDays(5), today.plusDays(5), null);

        assertFalse(loan.isOverdue(today));
    }

    @Test
    void isOverdue_falseWhenAlreadyReturned() {
        LocalDate today = LocalDate.of(2025, 1, 10);
        Loan loan = new Loan("L1", "U1", "B1",
                today.minusDays(40), today.minusDays(5), today.minusDays(3));

        assertFalse(loan.isOverdue(today));
    }
}