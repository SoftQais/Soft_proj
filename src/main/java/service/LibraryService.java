package service;

import library0.Book;
import library0.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core library operations for books (Sprint 1).
 */
public class LibraryService {

    private final BookRepository bookRepository;

    public LibraryService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * US1.3 Add book
     */
    public Book addBook(String title, String author, String isbn, int totalCopies) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required");
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN is required");
        }
        if (totalCopies <= 0) {
            throw new IllegalArgumentException("Total copies must be > 0");
        }

        // Check duplicate ISBN
        Optional<Book> existing = bookRepository.findByIsbn(isbn);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Book with same ISBN already exists");
        }

        String newId = generateNextBookId();

        Book book = new Book(
                newId,
                title.trim(),
                author.trim(),
                isbn.trim(),
                totalCopies,
                totalCopies
        );

        return bookRepository.save(book);
    }

    /**
     * US1.4 Search book by title
     */
    public List<Book> searchByTitle(String titlePart) {
        if (titlePart == null || titlePart.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return bookRepository.findByTitle(titlePart.trim());
    }

    /**
     * US1.4 Search book by author
     */
    public List<Book> searchByAuthor(String authorPart) {
        if (authorPart == null || authorPart.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return bookRepository.findByAuthor(authorPart.trim());
    }

    /**
     * US1.4 Search book by ISBN
     */
    public Optional<Book> searchByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Optional.empty();
        }
        return bookRepository.findByIsbn(isbn.trim());
    }

    // ---------- Helper for ID generation ----------

    private String generateNextBookId() {
        int max = 0;
        for (Book b : bookRepository.findAll()) {
            String id = b.getId();
            if (id != null && id.startsWith("B")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                    // تجاهل IDs الغريبة
                }
            }
        }
        return "B" + (max + 1);
    }
}