package service;

import library0.Loan;
import library0.repository.LoanRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Provides user borrow history (Sprint 5 – US5.2).
 */
public class HistoryService {

    private final LoanRepository loanRepository;
    private final TimeProvider timeProvider;

    public HistoryService(LoanRepository loanRepository, TimeProvider timeProvider) {
        this.loanRepository = loanRepository;
        this.timeProvider = timeProvider;
    }

    /**
     * Returns all loans for a given user (history).
     */
    public List<Loan> getUserHistory(String userId) {
        return loanRepository.findByUserId(userId);
    }

    /**
     * Convenience method to print history to console.
     */
    public void printUserHistory(String userId) {
        List<Loan> history = getUserHistory(userId);
        LocalDate today = timeProvider.today();

        System.out.println("---- Borrow History for User: " + userId + " ----");

        if (history.isEmpty()) {
            System.out.println("No borrow history.");
            return;
        }

        for (Loan l : history) {
            String status;
            if (l.isReturned()) {
                status = "Returned";
            } else if (l.isOverdue(today)) {
                status = "OVERDUE";
            } else {
                status = "Active";
            }

            System.out.println("Loan ID      : " + l.getId());
            System.out.println("Book ID      : " + l.getBookId());
            System.out.println("Borrow Date  : " + l.getBorrowDate());
            System.out.println("Due Date     : " + l.getDueDate());
            System.out.println("Returned Date: " + l.getReturnedDate());
            System.out.println("Status       : " + status);
            System.out.println("-----------------------------------");
        }
    }
}