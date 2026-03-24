package com.smartexpense.repository;

import com.smartexpense.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Notification> rowMapper = (rs, rowNum) -> {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setRead(rs.getBoolean("is_read"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        return n;
    };

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Notification save(Notification notification) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO notifications (user_id, message, type) VALUES (?, ?, ?)",
                new String[]{"id"}
            );
            ps.setLong(1, notification.getUserId());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getType());
            return ps;
        }, keyHolder);
        notification.setId(keyHolder.getKey().longValue());
        return notification;
    }

    public List<Notification> findByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50",
            rowMapper, userId
        );
    }

    public List<Notification> findUnreadByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC",
            rowMapper, userId
        );
    }

    public void markAsRead(Long id, Long userId) {
        jdbcTemplate.update("UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?", id, userId);
    }

    public void markAllAsRead(Long userId) {
        jdbcTemplate.update("UPDATE notifications SET is_read = TRUE WHERE user_id = ?", userId);
    }

    public int countUnread(Long userId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE",
            Integer.class, userId
        );
        return count != null ? count : 0;
    }
}
