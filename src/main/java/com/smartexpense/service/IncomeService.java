package com.smartexpense.service;

import com.smartexpense.model.Category;
import com.smartexpense.model.Income;
import com.smartexpense.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public Income addIncome(Long userId, BigDecimal amount, Category category,
                           String description, LocalDate date) {
        Income income = new Income(userId, amount, category, description, date);
        return incomeRepository.save(income);
    }

    public List<Income> getIncomesByUser(Long userId) {
        return incomeRepository.findByUserId(userId);
    }

    public BigDecimal getMonthlyTotal(Long userId, int month, int year) {
        return incomeRepository.getTotalByUserIdAndMonth(userId, month, year);
    }

    public void deleteIncome(Long id, Long userId) {
        incomeRepository.deleteById(id, userId);
    }
}
