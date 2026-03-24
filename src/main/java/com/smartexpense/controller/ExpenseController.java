package com.smartexpense.controller;

import com.smartexpense.model.Category;
import com.smartexpense.model.Expense;
import com.smartexpense.service.ExpenseService;
import com.smartexpense.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final NotificationService notificationService;

    public ExpenseController(ExpenseService expenseService,
                            NotificationService notificationService) {
        this.expenseService = expenseService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String listExpenses(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", Category.values());
        return "expenses";
    }

    @PostMapping("/add")
    public String addExpense(@RequestParam BigDecimal amount,
                            @RequestParam String category,
                            @RequestParam String description,
                            @RequestParam String date,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            Category cat = Category.valueOf(category);
            LocalDate expenseDate = LocalDate.parse(date);
            Expense expense = expenseService.addExpense(userId, amount, cat, description, expenseDate);

            // Notify if large expense
            if (expense.shouldNotify()) {
                notificationService.createAndBroadcast(userId,
                        expense.getNotificationMessage(), expense.getNotificationType());
            }

            redirectAttributes.addFlashAttribute("success", "Expense added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding expense: " + e.getMessage());
        }

        return "redirect:/expenses";
    }

    @PostMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        expenseService.deleteExpense(id, userId);
        redirectAttributes.addFlashAttribute("success", "Expense deleted.");
        return "redirect:/expenses";
    }
}
