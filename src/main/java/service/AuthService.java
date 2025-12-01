package service;

import library0.Role;
import library0.User;
import library0.repository.UserRepository;

import java.util.Optional;

/**
 * Simple authentication service for admin login/logout.
 */
public class AuthService {

    private final UserRepository userRepository;
    private User currentAdmin;


    public AuthService(UserRepository userRepository) {
    	 this.userRepository = userRepository;
	}

	/**
     * Tries to log in an admin using email and password.
     *
     * @return true if login success, false otherwise.
     */
    public boolean loginAdmin(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return false;
        }
        User u = userOpt.get();
        if (!u.getPassword().equals(password)) {
            return false;
        }
        if (u.getRole() != Role.ADMIN) {
            return false; // مش أدمن
        }
        currentAdmin = u;
        return true;
    }

    /**
     * Logs out current admin.
     */
    public void logout() {
        currentAdmin = null;
    }

    /**
     * @return true if an admin is currently logged in.
     */
    public boolean isAdminLoggedIn() {
        return currentAdmin != null;
    }

    /**
     * @return current admin or null if none.
     */
    public User getCurrentAdmin() {
        return currentAdmin;
    }
}