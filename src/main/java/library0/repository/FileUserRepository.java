package library0.repository;

import library0.Role;
import library0.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File-based implementation of UserRepository.
 * Format: id;name;email;role;password
 */
public class FileUserRepository implements UserRepository {

    private final Path filePath;

    public FileUserRepository(Path filePath) {
        this.filePath = filePath;
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            if (Files.notExists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize users file", e);
        }
    }

    @Override
    public synchronized User save(User user) {
        List<User> all = readAllInternal();

        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(user.getId())) {
                all.set(i, user);
                updated = true;
                break;
            }
        }

        if (!updated) {
            all.add(user);
        }

        writeAllInternal(all);
        return user;
    }

    @Override
    public synchronized Optional<User> findById(String id) {
        return readAllInternal().stream()
                .filter(u -> u.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    @Override
    public synchronized Optional<User> findByEmail(String email) {
        return readAllInternal().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public synchronized List<User> findAll() {
        return new ArrayList<>(readAllInternal());
    }

    public synchronized void deleteById(String id) {
        List<User> all = readAllInternal();
        boolean changed = all.removeIf(u -> u.getId().equals(id));
        if (changed) {
            writeAllInternal(all);
        }
    }

    // ---------- Helpers for file I/O ----------

    private List<User> readAllInternal() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                User u = parseLine(line);
                if (u != null) {
                    users.add(u);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read users file", e);
        }
        return users;
    }

    private void writeAllInternal(List<User> users) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            for (User u : users) {
                bw.write(formatLine(u));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write users file", e);
        }
    }

    private User parseLine(String line) {
        if (line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        if (parts.length != 5) {
            return null;
        }
        String id = parts[0];
        String name = parts[1];
        String email = parts[2];
        Role role = Role.valueOf(parts[3]);
        String password = parts[4];
        return new User(id, name, email, role, password);
    }

    private String formatLine(User u) {
        return String.join(";",
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole().name(),
                u.getPassword()
        );
    }
}