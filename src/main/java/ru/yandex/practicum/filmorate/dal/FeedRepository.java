package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedRepository {

    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;

    public void addEvent(Long userId, String eventType, String operation, Long entityId) {
        String sql = """
                INSERT INTO feed (timestamp, user_id, event_type, operation, entity_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbc.update(sql,
                System.currentTimeMillis(),
                userId,
                eventType,
                operation,
                entityId);
    }

    public List<Event> getFeedByUserId(Long userId) {
        String sql = """
                SELECT event_id, timestamp, user_id, event_type, operation, entity_id
                FROM feed
                WHERE user_id = ?
                ORDER BY timestamp ASC
                """;
        return jdbc.query(sql, eventRowMapper, userId);
    }
}
