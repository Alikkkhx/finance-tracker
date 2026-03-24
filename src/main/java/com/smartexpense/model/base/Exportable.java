package com.smartexpense.model.base;

/**
 * Interface for exportable entities.
 * Demonstrates OOP: Interface — implemented by Expense and Income.
 */
public interface Exportable {

    /**
     * Convert to CSV row format.
     */
    String toCsv();

    /**
     * Convert to JSON string format.
     */
    String toJson();

    /**
     * Get CSV header for this entity type.
     */
    static String getCsvHeader() {
        return "id,type,amount,category,description,date";
    }
}
