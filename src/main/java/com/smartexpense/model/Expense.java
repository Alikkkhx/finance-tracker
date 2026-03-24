package com.smartexpense.model;

import com.smartexpense.model.base.FinancialRecord;
import com.smartexpense.model.base.Notifiable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Expense entity — extends FinancialRecord (inheritance), implements Notifiable.
 * Demonstrates polymorphism: getRecordType() returns "EXPENSE".
 */
public class Expense extends FinancialRecord implements Notifiable {

    private static final BigDecimal LARGE_EXPENSE_THRESHOLD = new BigDecimal("500.00");

    public Expense() { super(); }

    public Expense(Long userId, BigDecimal amount, Category category,
                   String description, LocalDate date) {
        super(userId, amount, category, description, date);
    }

    /**
     * Polymorphic method — identifies this as an EXPENSE record.
     */
    @Override
    public String getRecordType() {
        return "EXPENSE";
    }

    @Override
    public String getNotificationMessage() {
        return String.format("New expense: %s for %s (%s)",
                getFormattedAmount(), category.getDisplayName(), description);
    }

    @Override
    public boolean shouldNotify() {
        return exceedsThreshold(LARGE_EXPENSE_THRESHOLD);
    }

    @Override
    public String getNotificationType() {
        if (exceedsThreshold(new BigDecimal("1000.00"))) return "ALERT";
        if (exceedsThreshold(LARGE_EXPENSE_THRESHOLD)) return "WARNING";
        return "INFO";
    }
}
