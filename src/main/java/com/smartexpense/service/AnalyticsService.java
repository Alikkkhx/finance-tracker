package com.smartexpense.service;

import com.smartexpense.dto.CategorySummary;
import com.smartexpense.dto.MonthlyTrend;
import com.smartexpense.model.Category;
import com.smartexpense.model.Expense;
import com.smartexpense.model.base.Analyzable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics service — implements Analyzable interface.
 * Demonstrates: Collections (Map, List, TreeMap, Stream), OOP (interface implementation).
 * The brain of the application — produces insights from expense data.
 */
@Service
public class AnalyticsService implements Analyzable<Expense> {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public AnalyticsService(ExpenseService expenseService, IncomeService incomeService) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
    }

    /**
     * Analyze expenses and produce detailed statistics.
     * Returns Map with summary data — demonstrates Map<String, Object>.
     */
    @Override
    public Map<String, Object> analyze(List<Expense> items) {
        Map<String, Object> result = new LinkedHashMap<>();

        BigDecimal total = items.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = items.isEmpty() ? BigDecimal.ZERO :
                total.divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);

        Optional<Expense> maxExpense = items.stream()
                .max(Comparator.comparing(Expense::getAmount));

        Optional<Expense> minExpense = items.stream()
                .min(Comparator.comparing(Expense::getAmount));

        result.put("totalExpenses", total);
        result.put("averageExpense", average);
        result.put("transactionCount", items.size());
        result.put("maxExpense", maxExpense.orElse(null));
        result.put("minExpense", minExpense.orElse(null));
        result.put("categorySummaries", getCategorySummaries(items));

        return result;
    }

    @Override
    public String getSummary(List<Expense> items) {
        Map<String, Object> analysis = analyze(items);
        return String.format("Total: %s | Avg: %s | Transactions: %d",
                analysis.get("totalExpenses"), analysis.get("averageExpense"),
                analysis.get("transactionCount"));
    }

    @Override
    public int getAnalyzedCount(List<Expense> items) {
        return items.size();
    }

    /**
     * Get category summaries with percentages.
     * Heavy use of: Stream, Collectors.groupingBy, Map, List.
     */
    public List<CategorySummary> getCategorySummaries(List<Expense> expenses) {
        if (expenses.isEmpty()) return new ArrayList<>();

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by category using Collectors — demonstrates Collections
        Map<Category, List<Expense>> grouped = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        List<CategorySummary> summaries = new ArrayList<>();

        for (Map.Entry<Category, List<Expense>> entry : grouped.entrySet()) {
            Category cat = entry.getKey();
            List<Expense> catExpenses = entry.getValue();

            BigDecimal catTotal = catExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? catTotal.doubleValue() / total.doubleValue() * 100 : 0;

            summaries.add(new CategorySummary(
                    cat.name(), cat.getDisplayName(), cat.getIcon(),
                    catTotal, Math.round(percentage * 10.0) / 10.0,
                    cat.getRecommendedPercentage(), catExpenses.size()
            ));
        }

        // Sort by total amount descending — demonstrates Comparator
        summaries.sort(Comparator.comparing(CategorySummary::getTotalAmount).reversed());
        return summaries;
    }

    /**
     * Get monthly trends for the last N months.
     * Demonstrates: TreeMap for ordered data, stream operations.
     */
    public List<MonthlyTrend> getMonthlyTrends(Long userId, int monthsBack) {
        List<MonthlyTrend> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = monthsBack - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int month = date.getMonthValue();
            int year = date.getYear();

            BigDecimal totalExpenses = expenseService.getMonthlyTotal(userId, month, year);
            BigDecimal totalIncome = incomeService.getMonthlyTotal(userId, month, year);

            MonthlyTrend trend = new MonthlyTrend(month, year, totalExpenses, totalIncome);
            trend.setMonthName(Month.of(month).name().substring(0, 3));
            trends.add(trend);
        }

        return trends;
    }

    /**
     * Calculate daily average spending for a given month.
     */
    public BigDecimal getDailyAverage(Long userId, int month, int year) {
        List<Expense> expenses = expenseService.getExpensesByMonth(userId, month, year);
        if (expenses.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count unique days with expenses
        long uniqueDays = expenses.stream()
                .map(Expense::getDate)
                .distinct()
                .count();

        return uniqueDays > 0 ? total.divide(BigDecimal.valueOf(uniqueDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }
}
