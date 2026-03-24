package com.smartexpense.service;

import com.smartexpense.model.Budget;
import com.smartexpense.model.Category;
import com.smartexpense.repository.BudgetRepository;
import com.smartexpense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    public Budget setBudget(Long userId, Category category, BigDecimal monthlyLimit,
                           int month, int year) {
        Budget budget = new Budget(userId, category, monthlyLimit, month, year);
        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsWithSpending(Long userId, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonth(userId, month, year);

        // Enrich with current spending — demonstrates polymorphism via Notifiable
        for (Budget budget : budgets) {
            if (budget.getCategory() != null) {
                BigDecimal spent = expenseRepository.getTotalByUserIdCategoryAndMonth(
                        userId, budget.getCategory().name(), month, year
                );
                budget.setCurrentSpending(spent);
            }
        }

        return budgets;
    }

    public void deleteBudget(Long id, Long userId) {
        budgetRepository.deleteById(id, userId);
    }
}
