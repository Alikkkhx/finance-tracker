package com.smartexpense.service;

import com.smartexpense.model.Category;
import com.smartexpense.model.Expense;
import com.smartexpense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for expense CRUD operations.
 * Demonstrates: Collections — List, Map, stream operations.
 */
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense addExpense(Long userId, BigDecimal amount, Category category,
                             String description, LocalDate date) {
        Expense expense = new Expense(userId, amount, category, description, date);
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByUser(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    public List<Expense> getExpensesByMonth(Long userId, int month, int year) {
        return expenseRepository.findByUserIdAndMonth(userId, month, year);
    }

    public List<Expense> getExpensesByDateRange(Long userId, LocalDate start, LocalDate end) {
        return expenseRepository.findByUserIdAndDateRange(userId, start, end);
    }

    public List<Expense> getRecentExpenses(Long userId, int limit) {
        return expenseRepository.findRecentByUserId(userId, limit);
    }

    public BigDecimal getMonthlyTotal(Long userId, int month, int year) {
        return expenseRepository.getTotalByUserIdAndMonth(userId, month, year);
    }

    public BigDecimal getCategoryMonthlyTotal(Long userId, Category category, int month, int year) {
        return expenseRepository.getTotalByUserIdCategoryAndMonth(userId, category.name(), month, year);
    }

    /**
     * Group expenses by category — demonstrates Map + stream + Collectors.
     */
    public Map<Category, List<Expense>> groupByCategory(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));
    }

    /**
     * Get category totals — demonstrates Map + stream + reducing.
     */
    public Map<Category, BigDecimal> getCategoryTotals(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                    Expense::getCategory,
                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
    }

    /**
     * Get top spending categories — demonstrates sorting + stream.
     */
    public List<Map.Entry<Category, BigDecimal>> getTopCategories(List<Expense> expenses, int limit) {
        Map<Category, BigDecimal> totals = getCategoryTotals(expenses);
        return totals.entrySet().stream()
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void updateExpense(Expense expense) {
        expenseRepository.update(expense);
    }

    public void deleteExpense(Long id, Long userId) {
        expenseRepository.deleteById(id, userId);
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }
}
