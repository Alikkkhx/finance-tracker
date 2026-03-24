package com.smartexpense.model.base;

/**
 * Interface for entities that can trigger notifications.
 * Demonstrates OOP: Interface.
 */
public interface Notifiable {

    /**
     * Get the notification message to display.
     */
    String getNotificationMessage();

    /**
     * Determine if this entity should trigger a notification.
     */
    boolean shouldNotify();

    /**
     * Get notification type (e.g., "WARNING", "INFO", "ALERT").
     */
    String getNotificationType();
}
