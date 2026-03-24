package com.smartexpense.service;

import com.smartexpense.dto.CategorySummary;
import com.smartexpense.dto.Recommendation;
import com.smartexpense.model.Budget;
import com.smartexpense.model.Category;
import com.smartexpense.model.Expense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart Recommendation Engine — the WOW factor.
 * Generates intelligent spending advice based on analysis.
 * Demonstrates: Collections (ArrayList, HashMap, sorting), business logic.
 */
@Service
public class RecommendationService {

    private final AnalyticsService analyticsService;
    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final ForecastService forecastService;

    public RecommendationService(AnalyticsService analyticsService,
                                 ExpenseService expenseService,
                                 BudgetService budgetService,
                                 ForecastService forecastService) {
        this.analyticsService = analyticsService;
        this.expenseService = expenseService;
        this.budgetService = budgetService;
        this.forecastService = forecastService;
    }

    /**
     * Generate all recommendations for a user.
     * Returns prioritized list — demonstrates ArrayList + sorting.
     */
    public List<Recommendation> generateRecommendations(Long userId) {
        List<Recommendation> recommendations = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        List<Expense> monthlyExpenses = expenseService.getExpensesByMonth(userId, month, year);
        List<CategorySummary> summaries = analyticsService.getCategorySummaries(monthlyExpenses);

        // 1. Category overspending recommendations
        recommendations.addAll(checkCategoryOverspending(summaries));

        // 2. Budget recommendations
        recommendations.addAll(checkBudgetAlerts(userId, month, year));

        // 3. Forecast-based recommendations
        recommendations.addAll(checkForecastAlerts(userId, month, year));

        // 4. Spending pattern recommendations
        recommendations.addAll(checkSpendingPatterns(monthlyExpenses));

        // 5. General tips
        if (recommendations.isEmpty()) {
            recommendations.add(new Recommendation(
                "You're doing great! 🎉",
                "Your spending patterns look healthy. Keep it up!",
                "TIP", "✅", 5
            ));
        }

        // Sort by priority — demonstrates Comparator
        recommendations.sort(Comparator.comparingInt(Recommendation::getPriority));
        return recommendations;
    }

    /**
     * Check if spending in any category exceeds recommended percentage.
     */
    private List<Recommendation> checkCategoryOverspending(List<CategorySummary> summaries) {
        List<Recommendation> recs = new ArrayList<>();

        for (CategorySummary summary : summaries) {
            if (summary.isOverRecommended()) {
                double excess = summary.getPercentage() - summary.getRecommendedPercentage();
                recs.add(new Recommendation(
                    summary.getIcon() + " High " + summary.getDisplayName() + " spending",
                    String.format("You're spending %.1f%% on %s — that's %.1f%% above the recommended %d%%. " +
                            "Consider reducing this category.",
                            summary.getPercentage(), summary.getDisplayName(),
                            excess, summary.getRecommendedPercentage()),
                    "WARNING", "⚠️",
                    excess > 20 ? 1 : 2
                ));
            }
        }

        return recs;
    }

    /**
     * Check budget limits and alert if near/exceeded.
     */
    private List<Recommendation> checkBudgetAlerts(Long userId, int month, int year) {
        List<Recommendation> recs = new ArrayList<>();
        List<Budget> budgets = budgetService.getBudgetsWithSpending(userId, month, year);

        for (Budget budget : budgets) {
            if (budget.isExceeded()) {
                recs.add(new Recommendation(
                    "🚨 Budget Exceeded: " + budget.getCategory().getDisplayName(),
                    String.format("You've spent $%.2f of your $%.2f budget for %s. You're $%.2f over!",
                            budget.getCurrentSpending().doubleValue(),
                            budget.getMonthlyLimit().doubleValue(),
                            budget.getCategory().getDisplayName(),
                            budget.getCurrentSpending().subtract(budget.getMonthlyLimit()).doubleValue()),
                    "ALERT", "🚨", 1
                ));
            } else if (budget.isNearLimit()) {
                recs.add(new Recommendation(
                    "⚡ Budget Warning: " + budget.getCategory().getDisplayName(),
                    String.format("You've used %.0f%% of your %s budget. Only $%.2f remaining.",
                            budget.getUsagePercentage(),
                            budget.getCategory().getDisplayName(),
                            budget.getRemaining().doubleValue()),
                    "WARNING", "⚡", 2
                ));
            }
        }

        return recs;
    }

    /**
     * Check forecast-based alerts.
     */
    private List<Recommendation> checkForecastAlerts(Long userId, int month, int year) {
        List<Recommendation> recs = new ArrayList<>();

        BigDecimal forecast = forecastService.forecastMonthlyTotal(userId, month, year);
        BigDecimal currentTotal = expenseService.getMonthlyTotal(userId, month, year);

        if (forecast.compareTo(currentTotal) > 0) {
            recs.add(new Recommendation(
                "📊 Monthly Forecast",
                String.format("Based on your spending pace, you'll likely spend $%.2f by month end. " +
                        "Currently at $%.2f.", forecast.doubleValue(), currentTotal.doubleValue()),
                "TIP", "📊", 3
            ));
        }

        return recs;
    }

    /**
     * Check for problematic spending patterns.
     */
    private List<Recommendation> checkSpendingPatterns(List<Expense> expenses) {
        List<Recommendation> recs = new ArrayList<>();

        if (expenses.isEmpty()) return recs;

        // Check for many small purchases (micro-spending)
        long smallPurchases = expenses.stream()
                .filter(e -> e.getAmount().compareTo(new BigDecimal("10")) < 0)
                .count();

        if (smallPurchases > 10) {
            BigDecimal smallTotal = expenses.stream()
                    .filter(e -> e.getAmount().compareTo(new BigDecimal("10")) < 0)
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            recs.add(new Recommendation(
                "🔍 Micro-spending detected",
                String.format("You have %d small purchases (under $10) totaling $%.2f. " +
                        "These add up quickly — try to batch your small purchases.",
                        smallPurchases, smallTotal.doubleValue()),
                "TIP", "🔍", 3
            ));
        }

        // Check for subscription-like recurring amounts
        Map<BigDecimal, Long> amountFrequency = expenses.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getAmount().setScale(2, RoundingMode.HALF_UP),
                    Collectors.counting()
                ));

        amountFrequency.entrySet().stream()
                .filter(e -> e.getValue() >= 3)
                .forEach(e -> recs.add(new Recommendation(
                    "🔄 Recurring expense detected",
                    String.format("You have %d transactions of $%.2f. Is this a subscription? " +
                            "Review if all subscriptions are necessary.",
                            e.getValue(), e.getKey().doubleValue()),
                    "TIP", "🔄", 4
                )));

        return recs;
    }
}
