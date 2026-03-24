package com.smartexpense.model.base;

import java.util.List;
import java.util.Map;

/**
 * Interface for analyzable data collections.
 * Demonstrates OOP: Interface with generics.
 */
public interface Analyzable<T> {

    /**
     * Analyze a list of items and produce summary statistics.
     */
    Map<String, Object> analyze(List<T> items);

    /**
     * Get a human-readable summary of analysis results.
     */
    String getSummary(List<T> items);

    /**
     * Get the total count of items analyzed.
     */
    int getAnalyzedCount(List<T> items);
}
