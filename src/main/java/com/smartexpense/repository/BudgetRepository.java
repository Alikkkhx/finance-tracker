package com.smartexpense.repository;

import com.smartexpense.model.Budget;
import com.smartexpense.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class BudgetRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Budget> rowMapper = (rs, rowNum) -> {
        Budget budget = new Budget();
        budget.setId(rs.getLong("id"));
        budget.setUserId(rs.getLong("user_id"));
        String cat = rs.getString("category");
        if (cat != null) budget.setCategory(Category.valueOf(cat));
        budget.setMonthlyLimit(rs.getBigDecimal("monthly_limit"));
        budget.setBudgetMonth(rs.getInt("budget_month"));
        budget.setBudgetYear(rs.getInt("budget_year"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) budget.setCreatedAt(ts.toLocalDateTime());
        return budget;
    };

    public BudgetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Budget save(Budget budget) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO budgets (user_id, category, monthly_limit, budget_month, budget_year) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE monthly_limit = VALUES(monthly_limit)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, budget.getUserId());
            ps.setString(2, budget.getCategory() != null ? budget.getCategory().name() : null);
            ps.setBigDecimal(3, budget.getMonthlyLimit());
            ps.setInt(4, budget.getBudgetMonth());
            ps.setInt(5, budget.getBudgetYear());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            budget.setId(keyHolder.getKey().longValue());
        }
        return budget;
    }

    public List<Budget> findByUserIdAndMonth(Long userId, int month, int year) {
        return jdbcTemplate.query(
            "SELECT * FROM budgets WHERE user_id = ? AND budget_month = ? AND budget_year = ?",
            rowMapper, userId, month, year
        );
    }

    public Optional<Budget> findByUserIdCategoryAndMonth(Long userId, Category category, int month, int year) {
        List<Budget> list = jdbcTemplate.query(
            "SELECT * FROM budgets WHERE user_id = ? AND category = ? AND budget_month = ? AND budget_year = ?",
            rowMapper, userId, category.name(), month, year
        );
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void deleteById(Long id, Long userId) {
        jdbcTemplate.update("DELETE FROM budgets WHERE id = ? AND user_id = ?", id, userId);
    }
}
