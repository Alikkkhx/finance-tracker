package com.smartexpense.dto;

import java.math.BigDecimal;

/**
 * DTO for category-wise expense summary.
 */
public class CategorySummary {

    private String category;
    private String displayName;
    private String icon;
    private BigDecimal totalAmount;
    private double percentage;
    private int recommendedPercentage;
    private int transactionCount;

    public CategorySummary() {}

    public CategorySummary(String category, String displayName, String icon,
                          BigDecimal totalAmount, double percentage,
                          int recommendedPercentage, int transactionCount) {
        this.category = category;
        this.displayName = displayName;
        this.icon = icon;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.recommendedPercentage = recommendedPercentage;
        this.transactionCount = transactionCount;
    }

    public boolean isOverRecommended() {
        return recommendedPercentage > 0 && percentage > recommendedPercentage;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public int getRecommendedPercentage() { return recommendedPercentage; }
    public void setRecommendedPercentage(int recommendedPercentage) { this.recommendedPercentage = recommendedPercentage; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}
