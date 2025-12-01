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
 * Handles borrowing, overdue detection, and fines (Sprint 2).
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
     * US2.1: Borrow a book for 28 days.
     * - Checks available copies.
     * - Checks user has no unpaid fines (from US2.3).
     * - Marks book as borrowed (availableCopies--).
     * - Creates Loan with dueDate = today + 28 days.
     */
    public Loan borrowBook(String userId, String bookId) {
        // حتى نضمن أن الغرامات محدثة حسب التاريخ الحالي
        generateFinesForOverdueLoans();

        if (hasUnpaidFines(userId)) {
            throw new IllegalStateException("User has unpaid fines. Please pay before borrowing.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies for this book.");
        }

        // Update book copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Create loan
        String newLoanId = generateNextLoanId();
        LocalDate today = timeProvider.today();
        LocalDate dueDate = today.plusDays(28);

        Loan loan = new Loan(newLoanId, userId, bookId, today, dueDate, null);
        loanRepository.save(loan);

        return loan;
    }

    /**
     * US2.2: Detect overdue books and create fines (once per overdue loan).
     * - If today > dueDate and loan not returned -> overdue.
     * - If no Fine exists for that loan -> create Fine with 10 NIS.
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
     * - Returns how much amount was actually applied.
     * - User can borrow only if outstandingFine(userId) == 0.
     */
    public int payFine(String userId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be > 0");
        }

        // قبل الدفع، حدّث الغرامات حسب الـ overdue
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

        return amount - remaining; // actually paid
    }

    /**
     * Get total outstanding fine for a user.
     */
    public int getOutstandingFine(String userId) {
        // حدّث الغرامات من الـ overdue أولاً
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