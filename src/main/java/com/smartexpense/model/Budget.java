package com.smartexpense.model;

import com.smartexpense.model.base.BaseEntity;
import com.smartexpense.model.base.Notifiable;

import java.math.BigDecimal;

/**
 * Budget entity — tracks monthly spending limits per category.
 * Implements Notifiable to trigger alerts when budget is exceeded.
 */
public class Budget extends BaseEntity implements Notifiable {

    private Long userId;
    private Category category;
    private BigDecimal monthlyLimit;
    private int budgetMonth;
    private int budgetYear;

    // Transient — not stored in DB, calculated at runtime
    private BigDecimal currentSpending = BigDecimal.ZERO;

    public Budget() { super(); }

    public Budget(Long userId, Category category, BigDecimal monthlyLimit,
                  int budgetMonth, int budgetYear) {
        super();
        this.userId = userId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.budgetMonth = budgetMonth;
        this.budgetYear = budgetYear;
    }

    @Override
    public String getEntityType() {
        return "BUDGET";
    }

    /**
     * Calculate usage percentage.
     */
    public double getUsagePercentage() {
        if (monthlyLimit.compareTo(BigDecimal.ZERO) == 0) return 0;
        return currentSpending.doubleValue() / monthlyLimit.doubleValue() * 100;
    }

    /**
     * Check if budget is exceeded.
     */
    public boolean isExceeded() {
        return currentSpending.compareTo(monthlyLimit) > 0;
    }

    /**
     * Check if budget is near limit (>80%).
     */
    public boolean isNearLimit() {
        return getUsagePercentage() >= 80 && !isExceeded();
    }

    public BigDecimal getRemaining() {
        return monthlyLimit.subtract(currentSpending);
    }

    @Override
    public String getNotificationMessage() {
        if (isExceeded()) {
            return String.format("⚠️ Budget EXCEEDED for %s! Spent %s of %s limit.",
                    category.getDisplayName(), currentSpending, monthlyLimit);
        }
        return String.format("Budget alert for %s: %.0f%% used (%s of %s).",
                category.getDisplayName(), getUsagePercentage(), currentSpending, monthlyLimit);
    }

    @Override
    public boolean shouldNotify() {
        return isExceeded() || isNearLimit();
    }

    @Override
    public String getNotificationType() {
        if (isExceeded()) return "ALERT";
        if (isNearLimit()) return "WARNING";
        return "INFO";
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    public int getBudgetMonth() { return budgetMonth; }
    public void setBudgetMonth(int budgetMonth) { this.budgetMonth = budgetMonth; }
    public int getBudgetYear() { return budgetYear; }
    public void setBudgetYear(int budgetYear) { this.budgetYear = budgetYear; }
    public BigDecimal getCurrentSpending() { return currentSpending; }
    public void setCurrentSpending(BigDecimal currentSpending) { this.currentSpending = currentSpending; }
}
