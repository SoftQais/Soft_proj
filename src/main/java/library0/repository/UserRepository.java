package library0.repository;

import library0.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for managing users.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    List<User> findAll();
}