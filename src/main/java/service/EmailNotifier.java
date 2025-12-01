package service;

import library0.User;

/**
 * Observer implementation that sends notifications via EmailServer.
 */
public class EmailNotifier implements Observer {

    private final EmailServer emailServer;

    public EmailNotifier(EmailServer emailServer) {
        this.emailServer = emailServer;
    }

    @Override
    public void notify(User user, String message) {
        String subject = "Library overdue reminder";
        emailServer.sendEmail(user.getEmail(), subject, message);
    }
}