package library0;

import library0.User;
import service.EmailNotifier;
import service.EmailServer;
import library0.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotifierTest {

    @Mock
    private EmailServer emailServer;

    @Test
    void notify_sendsEmailWithCorrectParameters() {
        EmailNotifier notifier = new EmailNotifier(emailServer);

        User user = new User("U1", "User", "user@lib.com", Role.CUSTOMER, "pwd");
        String message = "You have 2 overdue book(s).";

        notifier.notify(user, message);

        verify(emailServer).sendEmail(
                eq("user@lib.com"),
                eq("Library overdue reminder"),
                eq(message)
        );
    }
}