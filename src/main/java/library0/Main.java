package library0;

import library0.repository.UserRepository;
import service.AuthService;
import service.BorrowService;
import service.LibraryService;
import service.ReminderService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console UI for the Library System (Sprints 1–3).
 */
public class Main {

    public static void main(String[] args) {
        AppConfig cfg = new AppConfig();

        AuthService auth = cfg.authService();
        LibraryService lib = cfg.libraryService();
        BorrowService borrow = cfg.borrowService();
        ReminderService reminder = cfg.reminderService();
        UserRepository userRepo = cfg.userRepository();

        // demo customer (U2) – we use it in the customer menu
        User demoCustomer = userRepo.findByEmail("user@lib.com").orElse(null);

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== Library Management System ===");
        System.out.println("Admin:    admin@lib.com / admin123");
        System.out.println("Customer: user@lib.com  / user123 (demo user)");

        while (running) {
            System.out.println();

            if (!auth.isAdminLoggedIn()) {
                showGuestMenu();
            } else {
                showAdminMenu();
            }

            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();

            if (!auth.isAdminLoggedIn()) {
                // ------- Guest choices -------
                switch (choice) {
                    case "1":
                        handleAdminLogin(sc, auth);
                        break;
                    case "2":
                        handleCustomerMenu(sc, demoCustomer, lib, borrow);
                        break;
                    case "3":
                        handleSearchBooks(sc, lib);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } else {
                // ------- Admin choices -------
                switch (choice) {
                    case "1":
                        handleAddBook(sc, lib);
                        break;
                    case "2":
                        handleSearchBooks(sc, lib);
                        break;
                    case "3":
                        handleCustomerMenu(sc, demoCustomer, lib, borrow);
                        break;
                    case "4":
                        handleSendReminders(reminder);
                        break;
                    case "5":
                        auth.logout();
                        System.out.println("Admin logged out.");
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }

        System.out.println("Bye!");
    }

    // ===================== MENUS =====================

    private static void showGuestMenu() {
        System.out.println("---- Main Menu (Guest) ----");
        System.out.println("1) Admin login");
        System.out.println("2) Customer operations (demo user)");
        System.out.println("3) Search books");
        System.out.println("0) Exit");
    }

    private static void showAdminMenu() {
        System.out.println("---- Main Menu (Admin) ----");
        System.out.println("1) Add book");
        System.out.println("2) Search books");
        System.out.println("3) Customer operations (demo user)");
        System.out.println("4) Send overdue reminders");
        System.out.println("5) Logout");
        System.out.println("0) Exit");
    }

    // ===================== ADMIN HANDLERS =====================

    private static void handleAdminLogin(Scanner sc, AuthService auth) {
        System.out.print("Admin email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password: ");
        String pwd = sc.nextLine().trim();

        boolean ok = auth.loginAdmin(email, pwd);
        if (ok) {
            System.out.println("Login successful. Welcome, admin!");
        } else {
            System.out.println("Login failed. Check email/password or role.");
        }
    }

    private static void handleAddBook(Scanner sc, LibraryService lib) {
        try {
            System.out.print("Title: ");
            String title = sc.nextLine();
            System.out.print("Author: ");
            String author = sc.nextLine();
            System.out.print("ISBN: ");
            String isbn = sc.nextLine();
            System.out.print("Total copies: ");
            String copiesStr = sc.nextLine();
            int totalCopies = Integer.parseInt(copiesStr.trim());

            Book b = lib.addBook(title, author, isbn, totalCopies);
            System.out.println("Book added with ID: " + b.getId());
        } catch (Exception e) {
            System.out.println("Error adding book: " + e.getMessage());
        }
    }

    private static void handleSendReminders(ReminderService reminder) {
        System.out.println("Sending overdue reminders...");

        java.util.Map<String, Integer> result = reminder.sendOverdueReminders();

        System.out.println("Reminders sent to " + result.size() + " user(s).");
    }

    // ===================== SHARED: SEARCH BOOKS =====================

    private static void handleSearchBooks(Scanner sc, LibraryService lib) {
        System.out.println("Search by:");
        System.out.println("1) Title");
        System.out.println("2) Author");
        System.out.println("3) ISBN");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1":
                System.out.print("Title contains: ");
                String t = sc.nextLine();
                List<Book> byTitle = lib.searchByTitle(t);
                printBooks(byTitle);
                break;
            case "2":
                System.out.print("Author contains: ");
                String a = sc.nextLine();
                List<Book> byAuthor = lib.searchByAuthor(a);
                printBooks(byAuthor);
                break;
            case "3":
                System.out.print("ISBN: ");
                String isbn = sc.nextLine();
                Optional<Book> byIsbn = lib.searchByIsbn(isbn);
                if (byIsbn.isPresent()) {
                    printBooks(List.of(byIsbn.get()));
                } else {
                    System.out.println("No book found with that ISBN.");
                }
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No matching books.");
            return;
        }
        System.out.println("---- Results ----");
        for (Book b : books) {
            System.out.println("ID: " + b.getId() +
                    " | Title: " + b.getTitle() +
                    " | Author: " + b.getAuthor() +
                    " | ISBN: " + b.getIsbn() +
                    " | Available: " + b.getAvailableCopies() +
                    "/" + b.getTotalCopies());
        }
    }

    // ===================== CUSTOMER MENU (BORROW / FINES) =====================

    private static void handleCustomerMenu(Scanner sc,
                                           User customer,
                                           LibraryService lib,
                                           BorrowService borrow) {
        if (customer == null) {
            System.out.println("Demo customer not found.");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("---- Customer Menu (User: " + customer.getName() + ") ----");
            System.out.println("1) Borrow book by ID");
            System.out.println("2) Show outstanding fine");
            System.out.println("3) Pay fine");
            System.out.println("4) Search books");
            System.out.println("0) Back");
            System.out.print("Choice: ");
            String c = sc.nextLine().trim();

            switch (c) {
                case "1":
                    handleBorrowBook(sc, customer, borrow);
                    break;
                case "2":
                    int outstanding = borrow.getOutstandingFine(customer.getId());
                    System.out.println("Outstanding fine = " + outstanding + " NIS");
                    break;
                case "3":
                    handlePayFine(sc, customer, borrow);
                    break;
                case "4":
                    handleSearchBooks(sc, lib);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void handleBorrowBook(Scanner sc, User customer, BorrowService borrow) {
        System.out.print("Enter book ID to borrow (e.g., B1): ");
        String bookId = sc.nextLine().trim();
        try {
            Loan loan = borrow.borrowBook(customer.getId(), bookId);
            System.out.println("Borrowed successfully. Loan ID: " + loan.getId() +
                    " | Due date: " + loan.getDueDate());
        } catch (Exception e) {
            System.out.println("Error borrowing: " + e.getMessage());
        }
    }

    private static void handlePayFine(Scanner sc, User customer, BorrowService borrow) {
        int outstanding = borrow.getOutstandingFine(customer.getId());
        if (outstanding <= 0) {
            System.out.println("No outstanding fines.");
            return;
        }
        System.out.println("Current outstanding fine = " + outstanding + " NIS");
        System.out.print("Amount to pay: ");
        String amtStr = sc.nextLine().trim();
        try {
            int amount = Integer.parseInt(amtStr);
            int paid = borrow.payFine(customer.getId(), amount);
            System.out.println("Paid " + paid + " NIS.");
            int remaining = borrow.getOutstandingFine(customer.getId());
            System.out.println("Remaining fine = " + remaining + " NIS");
        } catch (Exception e) {
            System.out.println("Error paying fine: " + e.getMessage());
        }
    }
}