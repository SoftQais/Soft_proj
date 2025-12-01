package service;

import library0.User;

/**
 * Observer for notification events (e.g., overdue reminders).
 */
public interface Observer {

    /**
     * Called when a notification should be sent to a user.
     *
     * @param user    the target user
     * @param message the notification message
     */
    void notify(User user, String message);
}