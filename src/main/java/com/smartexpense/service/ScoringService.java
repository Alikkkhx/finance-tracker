package com.smartexpense.service;

import com.smartexpense.dto.CategorySummary;
import com.smartexpense.model.Budget;
import com.smartexpense.model.Expense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Scoring service — calculates financial discipline score (1-10).
 * Based on: budget adherence, category balance, spending consistency.
 */
@Service
public class ScoringService {

    private final ExpenseService expenseService;
    private final AnalyticsService analyticsService;
    private final BudgetService budgetService;

    public ScoringService(ExpenseService expenseService, AnalyticsService analyticsService,
                         BudgetService budgetService) {
        this.expenseService = expenseService;
        this.analyticsService = analyticsService;
        this.budgetService = budgetService;
    }

    /**
     * Calculate overall financial discipline score.
     * Returns a score from 1 to 10.
     */
    public int calculateScore(Long userId) {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        double totalScore = 0;
        int factors = 0;

        // Factor 1: Budget adherence (0-10)
        double budgetScore = calculateBudgetAdherence(userId, month, year);
        if (budgetScore >= 0) {
            totalScore += budgetScore;
            factors++;
        }

        // Factor 2: Category balance (0-10)
        double balanceScore = calculateCategoryBalance(userId, month, year);
        totalScore += balanceScore;
        factors++;

        // Factor 3: Spending consistency (0-10)
        double consistencyScore = calculateConsistency(userId, month, year);
        totalScore += consistencyScore;
        factors++;

        if (factors == 0) return 5; // Default neutral score
        int score = (int) Math.round(totalScore / factors);
        return Math.max(1, Math.min(10, score));
    }

    /**
     * Get a text description of the score.
     */
    public String getScoreLabel(int score) {
        if (score >= 9) return "Excellent";
        if (score >= 7) return "Good";
        if (score >= 5) return "Average";
        if (score >= 3) return "Needs Improvement";
        return "Critical";
    }

    /**
     * Get score color for UI.
     */
    public String getScoreColor(int score) {
        if (score >= 8) return "#10b981"; // green
        if (score >= 6) return "#f59e0b"; // amber
        if (score >= 4) return "#f97316"; // orange
        return "#ef4444"; // red
    }

    private double calculateBudgetAdherence(Long userId, int month, int year) {
        List<Budget> budgets = budgetService.getBudgetsWithSpending(userId, month, year);
        if (budgets.isEmpty()) return -1; // No budgets set

        double totalAdherence = 0;
        for (Budget budget : budgets) {
            double usage = budget.getUsagePercentage();
            if (usage <= 80) totalAdherence += 10;
            else if (usage <= 100) totalAdherence += 7;
            else if (usage <= 120) totalAdherence += 4;
            else totalAdherence += 1;
        }

        return totalAdherence / budgets.size();
    }

    private double calculateCategoryBalance(Long userId, int month, int year) {
        List<Expense> expenses = expenseService.getExpensesByMonth(userId, month, year);
        List<CategorySummary> summaries = analyticsService.getCategorySummaries(expenses);

        if (summaries.isEmpty()) return 5;

        int overRecommendedCount = 0;
        for (CategorySummary summary : summaries) {
            if (summary.isOverRecommended()) overRecommendedCount++;
        }

        double ratio = (double) overRecommendedCount / summaries.size();
        return 10 * (1 - ratio);
    }

    private double calculateConsistency(Long userId, int month, int year) {
        List<Expense> expenses = expenseService.getExpensesByMonth(userId, month, year);
        if (expenses.size() < 3) return 5;

        // Check if spending is spread evenly or bursty
        Map<Integer, BigDecimal> dailyTotals = new HashMap<>();
        for (Expense expense : expenses) {
            int day = expense.getDate().getDayOfMonth();
            dailyTotals.merge(day, expense.getAmount(), BigDecimal::add);
        }

        if (dailyTotals.size() < 2) return 5;

        // Calculate coefficient of variation
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : dailyTotals.values()) sum = sum.add(v);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(dailyTotals.size()), 4, RoundingMode.HALF_UP);

        if (mean.compareTo(BigDecimal.ZERO) == 0) return 5;

        BigDecimal varianceSum = BigDecimal.ZERO;
        for (BigDecimal v : dailyTotals.values()) {
            BigDecimal diff = v.subtract(mean);
            varianceSum = varianceSum.add(diff.multiply(diff));
        }
        BigDecimal variance = varianceSum.divide(BigDecimal.valueOf(dailyTotals.size()), 4, RoundingMode.HALF_UP);
        double stdDev = Math.sqrt(variance.doubleValue());
        double cv = stdDev / mean.doubleValue();

        // Low CV = consistent spending = higher score
        if (cv < 0.3) return 10;
        if (cv < 0.6) return 8;
        if (cv < 1.0) return 6;
        if (cv < 1.5) return 4;
        return 2;
    }
}
