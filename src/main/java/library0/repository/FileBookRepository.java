package library0.repository;

import library0.Book;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File-based implementation of BookRepository.
 * Stores data in a simple text file: id;title;author;isbn;totalCopies;availableCopies
 */
public class FileBookRepository implements BookRepository {

    private final Path filePath;

    public FileBookRepository(Path filePath) {
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
            throw new RuntimeException("Failed to initialize books file", e);
        }
    }

    @Override
    public synchronized Book save(Book book) {
        // Load all books
        List<Book> all = readAllInternal();

        // Check if exists → replace
        boolean updated = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(book.getId())) {
                all.set(i, book);
                updated = true;
                break;
            }
        }

        if (!updated) {
            all.add(book);
        }

        writeAllInternal(all);
        return book;
    }

    @Override
    public synchronized List<Book> findAll() {
        return new ArrayList<>(readAllInternal());
    }

    @Override
    public synchronized Optional<Book> findById(String id) {
        return readAllInternal().stream()
                .filter(b -> b.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    @Override
    public synchronized List<Book> findByTitle(String titlePart) {
        String pattern = titlePart.toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : readAllInternal()) {
            if (b.getTitle().toLowerCase().contains(pattern)) {
                result.add(b);
            }
        }
        return result;
    }

    @Override
    public synchronized List<Book> findByAuthor(String authorPart) {
        String pattern = authorPart.toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : readAllInternal()) {
            if (b.getAuthor().toLowerCase().contains(pattern)) {
                result.add(b);
            }
        }
        return result;
    }

    @Override
    public synchronized Optional<Book> findByIsbn(String isbn) {
        return readAllInternal().stream()
                .filter(b -> b.getIsbn().equalsIgnoreCase(isbn))
                .findFirst();
    }

    // ---------- Helper methods for file I/O ----------

    private List<Book> readAllInternal() {
        List<Book> books = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                Book b = parseLine(line);
                if (b != null) {
                    books.add(b);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read books file", e);
        }
        return books;
    }

    private void writeAllInternal(List<Book> books) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            for (Book b : books) {
                bw.write(formatLine(b));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write books file", e);
        }
    }

    private Book parseLine(String line) {
        if (line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        if (parts.length != 6) {
            return null; // or throw
        }
        String id = parts[0];
        String title = parts[1];
        String author = parts[2];
        String isbn = parts[3];
        int totalCopies = Integer.parseInt(parts[4]);
        int availableCopies = Integer.parseInt(parts[5]);
        return new Book(id, title, author, isbn, totalCopies, availableCopies);
    }

    private String formatLine(Book b) {
        return String.join(";",
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getIsbn(),
                String.valueOf(b.getTotalCopies()),
                String.valueOf(b.getAvailableCopies())
        );
    }
}