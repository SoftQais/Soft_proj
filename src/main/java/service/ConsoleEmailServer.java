package service;

/**
 * Simple EmailServer that prints emails to the console.
 */
public class ConsoleEmailServer implements EmailServer {

    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("---- EMAIL ----");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("----------------");
    }
}