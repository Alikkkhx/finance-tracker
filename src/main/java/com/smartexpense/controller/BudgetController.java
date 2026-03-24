package com.smartexpense.controller;

import com.smartexpense.model.Budget;
import com.smartexpense.model.Category;
import com.smartexpense.service.BudgetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public String budgetPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        LocalDate now = LocalDate.now();
        List<Budget> budgets = budgetService.getBudgetsWithSpending(
                userId, now.getMonthValue(), now.getYear());

        model.addAttribute("budgets", budgets);
        model.addAttribute("categories", Category.values());
        model.addAttribute("currentMonth", now.getMonth().name());
        model.addAttribute("currentYear", now.getYear());
        return "budget";
    }

    @PostMapping("/set")
    public String setBudget(@RequestParam String category, @RequestParam BigDecimal monthlyLimit,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            LocalDate now = LocalDate.now();
            budgetService.setBudget(userId, Category.valueOf(category), monthlyLimit,
                    now.getMonthValue(), now.getYear());
            redirectAttributes.addFlashAttribute("success", "Budget set successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/budget";
    }

    @PostMapping("/delete/{id}")
    public String deleteBudget(@PathVariable Long id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        budgetService.deleteBudget(id, userId);
        redirectAttributes.addFlashAttribute("success", "Budget deleted.");
        return "redirect:/budget";
    }
}
