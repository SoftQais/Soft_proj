package library0.repository;

import library0.Loan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File-based implementation of LoanRepository.
 * Format per line:
 * id;userId;bookId;borrowDate;dueDate;returnedDate
 * Dates are ISO-8601 (e.g., 2025-12-01). returnedDate may be empty.
 */
public class FileLoanRepository implements LoanRepository {

    private final Path filePath;

    public FileLoanRepository(Path filePath) {
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
            throw new RuntimeException("Failed to initialize loans file", e);
        }
    }

    @Override
    public synchronized Loan save(Loan loan) {
        List<Loan> all = readAllInternal();
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(loan.getId())) {
                all.set(i, loan);
                updated = true;
                break;
            }
        }
        if (!updated) {
            all.add(loan);
        }
        writeAllInternal(all);
        return loan;
    }

    @Override
    public synchronized List<Loan> findAll() {
        return new ArrayList<>(readAllInternal());
    }

    @Override
    public synchronized Optional<Loan> findById(String id) {
        return readAllInternal().stream()
                .filter(l -> l.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    @Override
    public synchronized List<Loan> findByUserId(String userId) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : readAllInternal()) {
            if (l.getUserId().equalsIgnoreCase(userId)) {
                result.add(l);
            }
        }
        return result;
    }

    // ---------- Helpers ----------

    private List<Loan> readAllInternal() {
        List<Loan> loans = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                Loan l = parseLine(line);
                if (l != null) {
                    loans.add(l);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read loans file", e);
        }
        return loans;
    }

    private void writeAllInternal(List<Loan> loans) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            for (Loan l : loans) {
                bw.write(formatLine(l));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write loans file", e);
        }
    }

    private Loan parseLine(String line) {
        if (line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        if (parts.length != 6) {
            return null;
        }
        String id = parts[0];
        String userId = parts[1];
        String bookId = parts[2];
        LocalDate borrowDate = LocalDate.parse(parts[3]);
        LocalDate dueDate = LocalDate.parse(parts[4]);
        LocalDate returnedDate = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);
        return new Loan(id, userId, bookId, borrowDate, dueDate, returnedDate);
    }

    private String formatLine(Loan l) {
        String returned = (l.getReturnedDate() == null) ? "" : l.getReturnedDate().toString();
        return String.join(";",
                l.getId(),
                l.getUserId(),
                l.getBookId(),
                l.getBorrowDate().toString(),
                l.getDueDate().toString(),
                returned
        );
    }
}