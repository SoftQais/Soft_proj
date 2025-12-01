package service;

/**
 * Abstraction for sending emails.
 * This can be mocked in tests using Mockito.
 */
public interface EmailServer {

    void sendEmail(String to, String subject, String body);
}