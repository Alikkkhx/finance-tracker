package com.smartexpense.controller;

import com.smartexpense.dto.CategorySummary;
import com.smartexpense.dto.MonthlyTrend;
import com.smartexpense.dto.Recommendation;
import com.smartexpense.model.Budget;
import com.smartexpense.model.Expense;
import com.smartexpense.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final AnalyticsService analyticsService;
    private final RecommendationService recommendationService;
    private final ScoringService scoringService;
    private final BudgetService budgetService;
    private final ForecastService forecastService;
    private final NotificationService notificationService;

    public DashboardController(ExpenseService expenseService, IncomeService incomeService,
                              AnalyticsService analyticsService,
                              RecommendationService recommendationService,
                              ScoringService scoringService, BudgetService budgetService,
                              ForecastService forecastService,
                              NotificationService notificationService) {
        this.expenseService = expenseService;
        this.incomeService = incomeService;
        this.analyticsService = analyticsService;
        this.recommendationService = recommendationService;
        this.scoringService = scoringService;
        this.budgetService = budgetService;
        this.forecastService = forecastService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        // Monthly totals
        BigDecimal totalExpenses = expenseService.getMonthlyTotal(userId, month, year);
        BigDecimal totalIncome = incomeService.getMonthlyTotal(userId, month, year);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        // Recent expenses
        List<Expense> recentExpenses = expenseService.getRecentExpenses(userId, 5);

        // Category summaries
        List<Expense> monthlyExpenses = expenseService.getExpensesByMonth(userId, month, year);
        List<CategorySummary> categorySummaries = analyticsService.getCategorySummaries(monthlyExpenses);

        // Monthly trends (last 6 months)
        List<MonthlyTrend> trends = analyticsService.getMonthlyTrends(userId, 6);

        // Smart recommendations
        List<Recommendation> recommendations = recommendationService.generateRecommendations(userId);

        // Financial score
        int score = scoringService.calculateScore(userId);
        String scoreLabel = scoringService.getScoreLabel(score);
        String scoreColor = scoringService.getScoreColor(score);

        // Forecast
        BigDecimal forecast = forecastService.forecastMonthlyTotal(userId, month, year);
        BigDecimal dailyBurn = forecastService.getDailyBurnRate(userId, month, year);

        // Budgets
        List<Budget> budgets = budgetService.getBudgetsWithSpending(userId, month, year);

        // Notification count
        int unreadNotifications = notificationService.getUnreadCount(userId);

        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("netSavings", netSavings);
        model.addAttribute("recentExpenses", recentExpenses);
        model.addAttribute("categorySummaries", categorySummaries);
        model.addAttribute("trends", trends);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("score", score);
        model.addAttribute("scoreLabel", scoreLabel);
        model.addAttribute("scoreColor", scoreColor);
        model.addAttribute("forecast", forecast);
        model.addAttribute("dailyBurn", dailyBurn);
        model.addAttribute("budgets", budgets);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("currentMonth", now.getMonth().name());

        return "dashboard";
    }
}
