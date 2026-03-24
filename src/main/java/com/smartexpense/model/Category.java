package com.smartexpense.model;

/**
 * Enum for expense/income categories.
 * Each category has a display name, icon, and recommended spending percentage.
 */
public enum Category {

    FOOD("Food & Dining", "🍔", 30),
    TRANSPORT("Transport", "🚗", 15),
    ENTERTAINMENT("Entertainment", "🎬", 10),
    UTILITIES("Utilities & Bills", "💡", 15),
    HEALTH("Health & Fitness", "🏥", 10),
    SHOPPING("Shopping", "🛍️", 10),
    EDUCATION("Education", "📚", 5),
    SUBSCRIPTIONS("Subscriptions", "📱", 5),
    SALARY("Salary", "💰", 0),
    FREELANCE("Freelance", "💻", 0),
    INVESTMENT("Investment", "📈", 0),
    OTHER("Other", "📦", 0);

    private final String displayName;
    private final String icon;
    private final int recommendedPercentage;

    Category(String displayName, String icon, int recommendedPercentage) {
        this.displayName = displayName;
        this.icon = icon;
        this.recommendedPercentage = recommendedPercentage;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public int getRecommendedPercentage() { return recommendedPercentage; }

    public boolean isExpenseCategory() {
        return this != SALARY && this != FREELANCE && this != INVESTMENT;
    }

    public boolean isIncomeCategory() {
        return this == SALARY || this == FREELANCE || this == INVESTMENT || this == OTHER;
    }
}
