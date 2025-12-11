package service;

import library0.Book;
import library0.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private BookRepository bookRepository;

    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService(bookRepository);
    }

    @Test
    void addBook_savesBookAndReturnsIt() {
        Book saved = new Book("B1", "Title", "Author", "123", 3, 3);
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        Book result = libraryService.addBook("Title", "Author", "123", 3);

        assertEquals("B1", result.getId());
        assertEquals("Title", result.getTitle());
        assertEquals("Author", result.getAuthor());
        assertEquals("123", result.getIsbn());
        assertEquals(3, result.getTotalCopies());
    }

   /* @Test
    void searchByTitle_filtersIgnoringCase() {
        Book b1 = new Book("B1", "Java Programming", "X", "111", 1, 1);
        Book b2 = new Book("B2", "Python Basics", "Y", "222", 1, 1);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(b1, b2));

        List<Book> result = libraryService.searchByTitle("java programming");

        assertEquals(1, result.size());
        assertEquals("B1", result.get(0).getId());
    }
*/
    @Test
    void searchByAuthor_returnsEmptyWhenNoMatch() {
        Book b1 = new Book("B1", "A", "Author1", "111", 1, 1);

        lenient().when(bookRepository.findAll())
                .thenReturn(Collections.singletonList(b1));

        List<Book> result = libraryService.searchByAuthor("Other");

        assertTrue(result.isEmpty());
    }

    @Test
    void searchByIsbn_returnsBookWhenExists() {
        Book b1 = new Book("B1", "A", "Author", "123", 1, 1);
        when(bookRepository.findByIsbn("123"))
                .thenReturn(Optional.of(b1));

        Optional<Book> result = libraryService.searchByIsbn("123");

        assertTrue(result.isPresent());
        assertEquals("B1", result.get().getId());
    }

    @Test
    void searchByIsbn_returnsEmptyWhenNotFound() {
        when(bookRepository.findByIsbn("999"))
                .thenReturn(Optional.empty());

        Optional<Book> result = libraryService.searchByIsbn("999");

        assertFalse(result.isPresent());
    }
}