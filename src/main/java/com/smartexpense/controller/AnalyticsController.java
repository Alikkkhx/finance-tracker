package com.smartexpense.controller;

import com.smartexpense.dto.CategorySummary;
import com.smartexpense.dto.MonthlyTrend;
import com.smartexpense.dto.Recommendation;
import com.smartexpense.model.Expense;
import com.smartexpense.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final ExpenseService expenseService;
    private final AnalyticsService analyticsService;
    private final RecommendationService recommendationService;
    private final ForecastService forecastService;
    private final ScoringService scoringService;

    public AnalyticsController(ExpenseService expenseService, AnalyticsService analyticsService,
                              RecommendationService recommendationService,
                              ForecastService forecastService, ScoringService scoringService) {
        this.expenseService = expenseService;
        this.analyticsService = analyticsService;
        this.recommendationService = recommendationService;
        this.forecastService = forecastService;
        this.scoringService = scoringService;
    }

    @GetMapping
    public String analytics(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        List<Expense> monthlyExpenses = expenseService.getExpensesByMonth(userId, month, year);

        // Full analysis — demonstrates Analyzable interface
        Map<String, Object> analysis = analyticsService.analyze(monthlyExpenses);

        // Category summaries
        List<CategorySummary> categorySummaries = analyticsService.getCategorySummaries(monthlyExpenses);

        // Monthly trends
        List<MonthlyTrend> trends = analyticsService.getMonthlyTrends(userId, 6);

        // Recommendations
        List<Recommendation> recommendations = recommendationService.generateRecommendations(userId);

        // Score
        int score = scoringService.calculateScore(userId);

        // Forecast
        BigDecimal forecast = forecastService.forecastMonthlyTotal(userId, month, year);
        BigDecimal dailyAverage = analyticsService.getDailyAverage(userId, month, year);

        model.addAttribute("analysis", analysis);
        model.addAttribute("categorySummaries", categorySummaries);
        model.addAttribute("trends", trends);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("score", score);
        model.addAttribute("scoreLabel", scoringService.getScoreLabel(score));
        model.addAttribute("scoreColor", scoringService.getScoreColor(score));
        model.addAttribute("forecast", forecast);
        model.addAttribute("dailyAverage", dailyAverage);
        model.addAttribute("currentMonth", now.getMonth().name());

        return "analytics";
    }
}
