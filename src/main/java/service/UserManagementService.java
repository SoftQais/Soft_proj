package service;

import library0.Loan;
import library0.Role;
import library0.User;
import library0.repository.LoanRepository;
import library0.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Handles user registration and unregistration (Sprint 4).
 */
public class UserManagementService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public UserManagementService(UserRepository userRepository,
                                 LoanRepository loanRepository) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * US4.1 Register user as CUSTOMER.
     */
    public User registerUser(String id, String name, String email, String password) {

        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("ID is required.");

        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name is required.");

        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email is required.");

        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException("Password is required.");

        // check duplicate email
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User user = new User(id.trim(), name.trim(), email.trim(), Role.CUSTOMER, password.trim());
        return userRepository.save(user);
    }

    /**
     * US4.2 Unregister user:
     *  - User must exist.
     *  - User must have no active (not returned) loans.
     *
     * @return true if user deleted, false if user has active loans.
     */
    public boolean unregisterUser(String userId) {

        // 1) Check user exists
        Optional<User> existing = userRepository.findById(userId);
        if (!existing.isPresent()) {
            throw new IllegalArgumentException("User not found.");
        }

        // 2) Check if user has any active loans (not returned)
        List<Loan> loans = loanRepository.findByUserId(userId);

        for (Loan loan : loans) {
            if (!loan.isReturned()) {
                // can't delete user with an active loan
                return false;
            }
        }

        // 3) Delete user
        userRepository.deleteById(userId);
        return true;
    }
}