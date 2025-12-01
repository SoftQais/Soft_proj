package library0;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Loan entity: represents a book borrowed by a user.
 */
public class Loan {

    private final String id;        // e.g., "L1"
    private final String userId;    // U2, ...
    private final String bookId;    // B1, ...
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnedDate; // null if not yet returned

    public Loan(String id,
                String userId,
                String bookId,
                LocalDate borrowDate,
                LocalDate dueDate,
                LocalDate returnedDate) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnedDate = returnedDate;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getBookId() {
        return bookId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    public boolean isReturned() {
        return returnedDate != null;
    }

    public boolean isOverdue(LocalDate today) {
        return !isReturned() && today.isAfter(dueDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        Loan loan = (Loan) o;
        return Objects.equals(id, loan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}