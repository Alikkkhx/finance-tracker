package com.smartexpense.repository;

import com.smartexpense.model.Category;
import com.smartexpense.model.Income;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class IncomeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Income> rowMapper = (rs, rowNum) -> {
        Income income = new Income();
        income.setId(rs.getLong("id"));
        income.setUserId(rs.getLong("user_id"));
        income.setAmount(rs.getBigDecimal("amount"));
        income.setCategory(Category.valueOf(rs.getString("category")));
        income.setDescription(rs.getString("description"));
        income.setDate(rs.getDate("income_date").toLocalDate());
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) income.setCreatedAt(ts.toLocalDateTime());
        return income;
    };

    public IncomeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Income save(Income income) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO incomes (user_id, amount, category, description, income_date) VALUES (?, ?, ?, ?, ?)",
                new String[]{"id"}
            );
            ps.setLong(1, income.getUserId());
            ps.setBigDecimal(2, income.getAmount());
            ps.setString(3, income.getCategory().name());
            ps.setString(4, income.getDescription());
            ps.setDate(5, Date.valueOf(income.getDate()));
            return ps;
        }, keyHolder);
        income.setId(keyHolder.getKey().longValue());
        return income;
    }

    public List<Income> findByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT * FROM incomes WHERE user_id = ? ORDER BY income_date DESC", rowMapper, userId
        );
    }

    public BigDecimal getTotalByUserIdAndMonth(Long userId, int month, int year) {
        BigDecimal total = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(amount), 0) FROM incomes WHERE user_id = ? AND MONTH(income_date) = ? AND YEAR(income_date) = ?",
            BigDecimal.class, userId, month, year
        );
        return total != null ? total : BigDecimal.ZERO;
    }

    public Optional<Income> findById(Long id) {
        List<Income> list = jdbcTemplate.query("SELECT * FROM incomes WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void deleteById(Long id, Long userId) {
        jdbcTemplate.update("DELETE FROM incomes WHERE id = ? AND user_id = ?", id, userId);
    }
}
