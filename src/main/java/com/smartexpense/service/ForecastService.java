package com.smartexpense.service;

import com.smartexpense.model.Expense;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Forecast service — predicts end-of-month spending.
 * Simple linear extrapolation (daily average × remaining days).
 */
@Service
public class ForecastService {

    private final ExpenseService expenseService;

    public ForecastService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Forecast total monthly spending based on current pace.
     */
    public BigDecimal forecastMonthlyTotal(Long userId, int month, int year) {
        List<Expense> expenses = expenseService.getExpensesByMonth(userId, month, year);
        if (expenses.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalSoFar = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate now = LocalDate.now();
        int dayOfMonth = now.getDayOfMonth();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        if (dayOfMonth >= daysInMonth) return totalSoFar;

        BigDecimal dailyAvg = totalSoFar.divide(BigDecimal.valueOf(dayOfMonth), 4, RoundingMode.HALF_UP);
        return dailyAvg.multiply(BigDecimal.valueOf(daysInMonth)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get the forecasted remaining spending.
     */
    public BigDecimal forecastRemaining(Long userId, int month, int year) {
        BigDecimal forecast = forecastMonthlyTotal(userId, month, year);
        BigDecimal current = expenseService.getMonthlyTotal(userId, month, year);
        BigDecimal remaining = forecast.subtract(current);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }

    /**
     * Calculate average daily burn rate.
     */
    public BigDecimal getDailyBurnRate(Long userId, int month, int year) {
        BigDecimal total = expenseService.getMonthlyTotal(userId, month, year);
        int dayOfMonth = LocalDate.now().getDayOfMonth();
        return dayOfMonth > 0 ? total.divide(BigDecimal.valueOf(dayOfMonth), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }
}
