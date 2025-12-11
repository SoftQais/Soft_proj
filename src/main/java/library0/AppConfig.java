package library0;

import library0.repository.*;
import service.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Application configuration: wires repositories and services.
 */
public class AppConfig {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;

    private final LibraryService libraryService;
    private final AuthService authService;
    private final BorrowService borrowService;
    private final ReminderService reminderService;
    private final UserManagementService userManagementService;
    private final HistoryService historyService;
    private final TimeProvider timeProvider;
    private final EmailServer emailServer;

    public AppConfig() {
        Path dataDir = Paths.get("src", "main", "resources", "data");

        this.bookRepository = new FileBookRepository(dataDir.resolve("books.txt"));
        this.userRepository = new FileUserRepository(dataDir.resolve("users.txt"));
        this.loanRepository = new FileLoanRepository(dataDir.resolve("loans.txt"));
        this.fineRepository = new FileFineRepository(dataDir.resolve("fines.txt"));

        seedDefaultAdmin();
        seedDemoCustomer();

        this.libraryService = new LibraryService(bookRepository);
        this.authService = new AuthService(userRepository);

        this.timeProvider = new SystemTimeProvider();
        this.borrowService = new BorrowService(bookRepository, loanRepository, fineRepository, timeProvider);

        this.reminderService = new ReminderService(loanRepository, userRepository, timeProvider);
        this.emailServer = new ConsoleEmailServer();
        Observer emailNotifier = new EmailNotifier(emailServer);
        this.reminderService.addObserver(emailNotifier);

        this.userManagementService = new UserManagementService(userRepository, loanRepository);

        this.historyService = new HistoryService(loanRepository, timeProvider);
    }

    private void seedDefaultAdmin() {
        String adminEmail = "admin@lib.com";
        Optional<User> existing = userRepository.findByEmail(adminEmail);
        if (!existing.isPresent()) {
            User admin = new User(
                    "U1",
                    "Admin",
                    adminEmail,
                    Role.ADMIN,
                    "admin123"
            );
            userRepository.save(admin);
        }
    }

    private void seedDemoCustomer() {
        String email = "user@lib.com";
        Optional<User> existing = userRepository.findByEmail(email);
        if (!existing.isPresent()) {
            User user = new User(
                    "U2",
                    "Demo User",
                    email,
                    Role.CUSTOMER,
                    "user123"
            );
            userRepository.save(user);
        }
    }

    public BookRepository bookRepository() {
        return bookRepository;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public LoanRepository loanRepository() {
        return loanRepository;
    }

    public FineRepository fineRepository() {
        return fineRepository;
    }

    public LibraryService libraryService() {
        return libraryService;
    }

    public AuthService authService() {
        return authService;
    }

    public BorrowService borrowService() {
        return borrowService;
    }

    public ReminderService reminderService() {
        return reminderService;
    }

    public UserManagementService userManagementService() {
        return userManagementService;
    }

    public HistoryService historyService() {
        return historyService;
    }

    public TimeProvider timeProvider() {
        return timeProvider;
    }

    public EmailServer emailServer() {
        return emailServer;
    }
}