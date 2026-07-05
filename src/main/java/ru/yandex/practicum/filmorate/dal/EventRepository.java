package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    private final JdbcTemplate jdbc;
    private final EventRowMapper mapper;

    public List<Event> getFeedByUserId(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp ASC, event_id ASC";
        return jdbc.query(sql, mapper, userId);
    }

    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        String sql = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long now = System.currentTimeMillis();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, now);
            ps.setLong(2, userId);
            ps.setString(3, eventType.name());
            ps.setString(4, operation.name());
            ps.setLong(5, entityId);
            return ps;
        }, keyHolder);
    }
}
