package com.smartexpense.controller;

import com.smartexpense.model.Category;
import com.smartexpense.model.Income;
import com.smartexpense.service.IncomeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public String listIncomes(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<Income> incomes = incomeService.getIncomesByUser(userId);
        model.addAttribute("incomes", incomes);
        model.addAttribute("categories", Category.values());
        return "incomes";
    }

    @PostMapping("/add")
    public String addIncome(@RequestParam BigDecimal amount,
                           @RequestParam String category,
                           @RequestParam String description,
                           @RequestParam String date,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            Category cat = Category.valueOf(category);
            incomeService.addIncome(userId, amount, cat, description, LocalDate.parse(date));
            redirectAttributes.addFlashAttribute("success", "Income added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/incomes";
    }

    @PostMapping("/delete/{id}")
    public String deleteIncome(@PathVariable Long id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        incomeService.deleteIncome(id, userId);
        redirectAttributes.addFlashAttribute("success", "Income deleted.");
        return "redirect:/incomes";
    }
}
