package com.smartexpense.dto;

/**
 * DTO for smart recommendations.
 */
public class Recommendation {

    private String title;
    private String message;
    private String type; // SAVINGS, WARNING, TIP, ALERT
    private String icon;
    private int priority; // 1 = highest

    public Recommendation() {}

    public Recommendation(String title, String message, String type, String icon, int priority) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.icon = icon;
        this.priority = priority;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
