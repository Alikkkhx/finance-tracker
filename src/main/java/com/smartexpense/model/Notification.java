package com.smartexpense.model;

import com.smartexpense.model.base.BaseEntity;

/**
 * Notification entity for system alerts and messages.
 */
public class Notification extends BaseEntity {

    private Long userId;
    private String message;
    private String type; // INFO, WARNING, ALERT
    private boolean read;

    public Notification() { super(); }

    public Notification(Long userId, String message, String type) {
        super();
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.read = false;
    }

    @Override
    public String getEntityType() {
        return "NOTIFICATION";
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
