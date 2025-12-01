package library0.repository;

import library0.Fine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File-based implementation of FineRepository.
 * Format per line:
 * id;userId;loanId;totalAmount;paidAmount
 */
public class FileFineRepository implements FineRepository {

    private final Path filePath;

    public FileFineRepository(Path filePath) {
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
            throw new RuntimeException("Failed to initialize fines file", e);
        }
    }

    @Override
    public synchronized Fine save(Fine fine) {
        List<Fine> all = readAllInternal();
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(fine.getId())) {
                all.set(i, fine);
                updated = true;
                break;
            }
        }
        if (!updated) {
            all.add(fine);
        }
        writeAllInternal(all);
        return fine;
    }

    @Override
    public synchronized List<Fine> findAll() {
        return new ArrayList<>(readAllInternal());
    }

    @Override
    public synchronized List<Fine> findByUserId(String userId) {
        List<Fine> result = new ArrayList<>();
        for (Fine f : readAllInternal()) {
            if (f.getUserId().equalsIgnoreCase(userId)) {
                result.add(f);
            }
        }
        return result;
    }

    @Override
    public synchronized Optional<Fine> findByLoanId(String loanId) {
        return readAllInternal().stream()
                .filter(f -> f.getLoanId().equalsIgnoreCase(loanId))
                .findFirst();
    }

    // ---------- Helpers ----------

    private List<Fine> readAllInternal() {
        List<Fine> fines = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                Fine f = parseLine(line);
                if (f != null) {
                    fines.add(f);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read fines file", e);
        }
        return fines;
    }

    private void writeAllInternal(List<Fine> fines) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            for (Fine f : fines) {
                bw.write(formatLine(f));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write fines file", e);
        }
    }

    private Fine parseLine(String line) {
        if (line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        if (parts.length != 5) {
            return null;
        }
        String id = parts[0];
        String userId = parts[1];
        String loanId = parts[2];
        int totalAmount = Integer.parseInt(parts[3]);
        int paidAmount = Integer.parseInt(parts[4]);
        return new Fine(id, userId, loanId, totalAmount, paidAmount);
    }

    private String formatLine(Fine f) {
        return String.join(";",
                f.getId(),
                f.getUserId(),
                f.getLoanId(),
                String.valueOf(f.getTotalAmount()),
                String.valueOf(f.getPaidAmount())
        );
    }
}