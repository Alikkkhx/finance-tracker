package com.smartexpense.repository;

import com.smartexpense.model.Category;
import com.smartexpense.model.Expense;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based repository for Expense entity.
 * Demonstrates: JDBC + MySQL — complex queries, aggregations, date filtering.
 */
@Repository
public class ExpenseRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Expense> rowMapper = (rs, rowNum) -> {
        Expense expense = new Expense();
        expense.setId(rs.getLong("id"));
        expense.setUserId(rs.getLong("user_id"));
        expense.setAmount(rs.getBigDecimal("amount"));
        expense.setCategory(Category.valueOf(rs.getString("category")));
        expense.setDescription(rs.getString("description"));
        expense.setDate(rs.getDate("expense_date").toLocalDate());
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) expense.setCreatedAt(ts.toLocalDateTime());
        return expense;
    };

    public ExpenseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Expense save(Expense expense) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO expenses (user_id, amount, category, description, expense_date) VALUES (?, ?, ?, ?, ?)",
                new String[]{"id"}
            );
            ps.setLong(1, expense.getUserId());
            ps.setBigDecimal(2, expense.getAmount());
            ps.setString(3, expense.getCategory().name());
            ps.setString(4, expense.getDescription());
            ps.setDate(5, Date.valueOf(expense.getDate()));
            return ps;
        }, keyHolder);
        expense.setId(keyHolder.getKey().longValue());
        return expense;
    }

    public Optional<Expense> findById(Long id) {
        List<Expense> expenses = jdbcTemplate.query(
            "SELECT * FROM expenses WHERE id = ?", rowMapper, id
        );
        return expenses.isEmpty() ? Optional.empty() : Optional.of(expenses.get(0));
    }

    public List<Expense> findByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC", rowMapper, userId
        );
    }

    public List<Expense> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.query(
            "SELECT * FROM expenses WHERE user_id = ? AND expense_date BETWEEN ? AND ? ORDER BY expense_date DESC",
            rowMapper, userId, Date.valueOf(startDate), Date.valueOf(endDate)
        );
    }

    public List<Expense> findByUserIdAndCategory(Long userId, Category category) {
        return jdbcTemplate.query(
            "SELECT * FROM expenses WHERE user_id = ? AND category = ? ORDER BY expense_date DESC",
            rowMapper, userId, category.name()
        );
    }

    public List<Expense> findByUserIdAndMonth(Long userId, int month, int year) {
        return jdbcTemplate.query(
            "SELECT * FROM expenses WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ? ORDER BY expense_date DESC",
            rowMapper, userId, month, year
        );
    }

    public BigDecimal getTotalByUserIdAndMonth(Long userId, int month, int year) {
        BigDecimal total = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ?",
            BigDecimal.class, userId, month, year
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalByUserIdCategoryAndMonth(Long userId, String category, int month, int year) {
        BigDecimal total = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ? AND category = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ?",
            BigDecimal.class, userId, category, month, year
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    public void update(Expense expense) {
        jdbcTemplate.update(
            "UPDATE expenses SET amount = ?, category = ?, description = ?, expense_date = ? WHERE id = ? AND user_id = ?",
            expense.getAmount(), expense.getCategory().name(), expense.getDescription(),
            Date.valueOf(expense.getDate()), expense.getId(), expense.getUserId()
        );
    }

    public void deleteById(Long id, Long userId) {
        jdbcTemplate.update("DELETE FROM expenses WHERE id = ? AND user_id = ?", id, userId);
    }

    public int countByUserId(Long userId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM expenses WHERE user_id = ?", Integer.class, userId
        );
        return count != null ? count : 0;
    }

    public List<Expense> findRecentByUserId(Long userId, int limit) {
        return jdbcTemplate.query(
            "SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC LIMIT ?",
            rowMapper, userId, limit
        );
    }
}
