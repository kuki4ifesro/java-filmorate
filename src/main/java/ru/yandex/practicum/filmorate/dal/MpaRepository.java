package ru.yandex.practicum.filmorate.dal;

import jakarta.annotation.PostConstruct;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRepository {

    private final JdbcTemplate jdbcTemplate;

    public MpaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> getAllMpa() {
        String sql = "SELECT id, name FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Mpa(rs.getLong("id"), rs.getString("name"))
        );
    }

    public Optional<Mpa> getMpaById(Long id) {
        String sql = "SELECT id, name FROM mpa WHERE id = ?";
        try {
            Mpa mpa = jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new Mpa(rs.getLong("id"), rs.getString("name")),
                    id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @PostConstruct
    public void init() {
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mpa", Integer.class) == 0) {
            String sql = "INSERT INTO mpa (id, name) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sql,
                    List.of(
                            new Object[]{1L, "G"},
                            new Object[]{2L, "PG"},
                            new Object[]{3L, "PG-13"},
                            new Object[]{4L, "R"},
                            new Object[]{5L, "NC-17"}
                    )
            );
        }
    }
}
