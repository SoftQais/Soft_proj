package service;

import library0.Book;
import library0.Fine;
import library0.Loan;
import library0.repository.BookRepository;
import library0.repository.FineRepository;
import library0.repository.LoanRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles borrowing, overdue detection, and fines (Sprints 2 + 5).
 */
public class BorrowService {

    private static final int BOOK_FINE_AMOUNT = 10; // 10 NIS per overdue book

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;
    private final TimeProvider timeProvider;

    public BorrowService(BookRepository bookRepository,
                         LoanRepository loanRepository,
                         FineRepository fineRepository,
                         TimeProvider timeProvider) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.fineRepository = fineRepository;
        this.timeProvider = timeProvider;
    }

    /**
     * US2.1 + US5.1: Borrow a book for 28 days.
     *
     * Restrictions (US5.1):
     *  - User must have no unpaid fines.
     *  - User must have no overdue loans.
     *  - User must not have 3 active (not returned) loans.
     */
    public Loan borrowBook(String userId, String bookId) {

        // 0) Ensure fines are generated for overdue loans
        generateFinesForOverdueLoans();

        // 1) Restriction: user has unpaid fines
        if (hasUnpaidFines(userId)) {
            throw new IllegalStateException("User has unpaid fines. Please pay before borrowing.");
        }

        // 2) Restriction: user has overdue loans (not returned & due date passed)
        LocalDate today = timeProvider.today();
        List<Loan> userLoans = loanRepository.findByUserId(userId);
        for (Loan loan : userLoans) {
            if (loan.isOverdue(today)) {
                throw new IllegalStateException("User has overdue books. Cannot borrow until returned.");
            }
        }

        // 3) Restriction: user already has 3 active loans
        long activeLoans = userLoans.stream()
                .filter(l -> !l.isReturned())
                .count();

        if (activeLoans >= 3) {
            throw new IllegalStateException("User already has 3 active loans. Cannot borrow more.");
        }

        // 4) Check book availability
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies for this book.");
        }

        // 5) Update book copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // 6) Create loan
        String newLoanId = generateNextLoanId();
        LocalDate borrowDate = today;
        LocalDate dueDate = today.plusDays(28);

        Loan loan = new Loan(newLoanId, userId, bookId, borrowDate, dueDate, null);
        loanRepository.save(loan);

        return loan;
    }

    /**
     * US2.2: Detect overdue books and create fines (once per overdue loan).
     */
    public void generateFinesForOverdueLoans() {
        LocalDate today = timeProvider.today();
        List<Loan> allLoans = loanRepository.findAll();
        for (Loan loan : allLoans) {
            if (loan.isOverdue(today)) {
                Optional<Fine> existingFine = fineRepository.findByLoanId(loan.getId());
                if (!existingFine.isPresent()) {
                    String newFineId = generateNextFineId();
                    Fine fine = new Fine(
                            newFineId,
                            loan.getUserId(),
                            loan.getId(),
                            BOOK_FINE_AMOUNT,
                            0
                    );
                    fineRepository.save(fine);
                }
            }
        }
    }

    /**
     * Helper: get all overdue loans (for reporting, if needed).
     */
    public List<Loan> listOverdueLoans() {
        LocalDate today = timeProvider.today();
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loanRepository.findAll()) {
            if (loan.isOverdue(today)) {
                result.add(loan);
            }
        }
        return result;
    }

    /**
     * US2.3: Pay fines (full or partial) for a user.
     *
     * @return how much amount was actually applied.
     */
    public int payFine(String userId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be > 0");
        }

        generateFinesForOverdueLoans();

        int remaining = amount;
        List<Fine> fines = new ArrayList<>(fineRepository.findByUserId(userId));

        for (Fine fine : fines) {
            int outstanding = fine.getOutstandingAmount();
            if (outstanding <= 0) {
                continue;
            }
            int applied = fine.applyPayment(remaining);
            fineRepository.save(fine);
            remaining -= applied;
            if (remaining == 0) {
                break;
            }
        }

        return amount - remaining;
    }

    /**
     * Get total outstanding fine for a user.
     */
    public int getOutstandingFine(String userId) {
        generateFinesForOverdueLoans();

        int sum = 0;
        for (Fine f : fineRepository.findByUserId(userId)) {
            sum += f.getOutstandingAmount();
        }
        return sum;
    }

    // ---------- Helpers ----------

    private boolean hasUnpaidFines(String userId) {
        return getOutstandingFine(userId) > 0;
    }

    private String generateNextLoanId() {
        int max = 0;
        for (Loan l : loanRepository.findAll()) {
            String id = l.getId();
            if (id != null && id.startsWith("L")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "L" + (max + 1);
    }

    private String generateNextFineId() {
        int max = 0;
        for (Fine f : fineRepository.findAll()) {
            String id = f.getId();
            if (id != null && id.startsWith("F")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "F" + (max + 1);
    }
}