package com.smartexpense.dto;

import java.math.BigDecimal;

/**
 * DTO for monthly spending trend data.
 */
public class MonthlyTrend {

    private int month;
    private int year;
    private String monthName;
    private BigDecimal totalExpenses;
    private BigDecimal totalIncome;
    private BigDecimal netSavings;
    private int transactionCount;

    public MonthlyTrend() {}

    public MonthlyTrend(int month, int year, BigDecimal totalExpenses,
                       BigDecimal totalIncome) {
        this.month = month;
        this.year = year;
        this.totalExpenses = totalExpenses;
        this.totalIncome = totalIncome;
        this.netSavings = totalIncome.subtract(totalExpenses);
        this.monthName = java.time.Month.of(month).name();
    }

    public boolean isPositive() {
        return netSavings.compareTo(BigDecimal.ZERO) >= 0;
    }

    // Getters and Setters
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getNetSavings() { return netSavings; }
    public void setNetSavings(BigDecimal netSavings) { this.netSavings = netSavings; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}
