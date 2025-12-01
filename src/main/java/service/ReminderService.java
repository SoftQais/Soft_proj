package service;

import library0.Loan;
import library0.User;
import library0.repository.LoanRepository;
import library0.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * Service responsible for sending reminders to users with overdue books.
 */
public class ReminderService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final TimeProvider timeProvider;

    private final List<Observer> observers = new ArrayList<>();

    public ReminderService(LoanRepository loanRepository,
                           UserRepository userRepository,
                           TimeProvider timeProvider) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.timeProvider = timeProvider;
    }

    /**
     * Register an observer (e.g., EmailNotifier).
     */
    public void addObserver(Observer observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    /**
     * US3.1 Send reminder:
     *  - Detect overdue loans (> 28 days, not returned).
     *  - Group by user.
     *  - For each user with n overdue books, send:
     *      "You have n overdue book(s)."
     *
     * @return map of userId -> overdueCount (for reporting / tests).
     */
    public Map<String, Integer> sendOverdueReminders() {
        LocalDate today = timeProvider.today();
        List<Loan> loans = loanRepository.findAll();

        // count overdue per userId
        Map<String, Integer> overdueCountByUser = new HashMap<>();
        for (Loan loan : loans) {
            if (loan.isOverdue(today)) {
                overdueCountByUser.merge(loan.getUserId(), 1, Integer::sum);
            }
        }

        // send notifications via observers
        for (Map.Entry<String, Integer> entry : overdueCountByUser.entrySet()) {
            String userId = entry.getKey();
            int count = entry.getValue();

            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                continue;
            }
            User user = userOpt.get();
            String message = "You have " + count + " overdue book(s).";

            notifyObservers(user, message);
        }

        return overdueCountByUser;
    }

    private void notifyObservers(User user, String message) {
        for (Observer o : observers) {
            o.notify(user, message);
        }
    }
}