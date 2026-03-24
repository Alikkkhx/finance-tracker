package com.smartexpense.model.base;

import com.smartexpense.model.Category;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Abstract class for financial records (expenses and incomes).
 * Demonstrates OOP: Inheritance — Expense and Income extend this class.
 * Demonstrates OOP: Polymorphism — getRecordType() is overridden differently.
 */
public abstract class FinancialRecord extends BaseEntity implements Exportable {

    protected Long userId;
    protected BigDecimal amount;
    protected Category category;
    protected String description;
    protected LocalDate date;

    public FinancialRecord() {
        super();
    }

    public FinancialRecord(Long userId, BigDecimal amount, Category category,
                           String description, LocalDate date) {
        super();
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    /**
     * Polymorphic method — each subclass returns its own type.
     */
    public abstract String getRecordType();

    /**
     * Returns formatted amount with currency symbol.
     */
    public String getFormattedAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(amount);
    }

    /**
     * Check if amount exceeds a given threshold.
     */
    public boolean exceedsThreshold(BigDecimal threshold) {
        return amount.compareTo(threshold) > 0;
    }

    @Override
    public String getEntityType() {
        return getRecordType();
    }

    @Override
    public String toCsv() {
        return String.join(",",
                String.valueOf(id),
                getRecordType(),
                amount.toString(),
                category.name(),
                description != null ? "\"" + description + "\"" : "",
                date.toString()
        );
    }

    @Override
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"type\":\"%s\",\"amount\":%s,\"category\":\"%s\",\"description\":\"%s\",\"date\":\"%s\"}",
            id, getRecordType(), amount, category.name(),
            description != null ? description : "", date
        );
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
