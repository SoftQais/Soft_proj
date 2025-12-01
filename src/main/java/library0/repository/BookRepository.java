package library0.repository;

import library0.Book;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for managing books.
 */
public interface BookRepository {

    Book save(Book book);

    List<Book> findAll();

    Optional<Book> findById(String id);

    List<Book> findByTitle(String titlePart);

    List<Book> findByAuthor(String authorPart);

    Optional<Book> findByIsbn(String isbn);
}