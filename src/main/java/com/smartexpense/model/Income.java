package com.smartexpense.model;

import com.smartexpense.model.base.FinancialRecord;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Income entity — extends FinancialRecord (inheritance).
 * Demonstrates polymorphism: getRecordType() returns "INCOME".
 */
public class Income extends FinancialRecord {

    private String source;

    public Income() { super(); }

    public Income(Long userId, BigDecimal amount, Category category,
                  String description, LocalDate date) {
        super(userId, amount, category, description, date);
    }

    /**
     * Polymorphic method — identifies this as an INCOME record.
     */
    @Override
    public String getRecordType() {
        return "INCOME";
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
