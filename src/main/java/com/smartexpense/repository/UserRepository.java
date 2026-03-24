package com.smartexpense.repository;

import com.smartexpense.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based repository for User entity.
 * Demonstrates: JDBC + MySQL with JdbcTemplate.
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setMonthlyIncome(rs.getBigDecimal("monthly_income"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) user.setCreatedAt(ts.toLocalDateTime());
        return user;
    };

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (username, password, email, full_name, monthly_income) VALUES (?, ?, ?, ?, ?)",
                new String[]{"id"}
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getFullName());
            ps.setBigDecimal(5, user.getMonthlyIncome());
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = jdbcTemplate.query(
            "SELECT * FROM users WHERE username = ?", rowMapper, username
        );
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query(
            "SELECT * FROM users WHERE id = ?", rowMapper, id
        );
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", rowMapper);
    }

    public void update(User user) {
        jdbcTemplate.update(
            "UPDATE users SET email = ?, full_name = ?, monthly_income = ? WHERE id = ?",
            user.getEmail(), user.getFullName(), user.getMonthlyIncome(), user.getId()
        );
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username
        );
        return count != null && count > 0;
    }
}
