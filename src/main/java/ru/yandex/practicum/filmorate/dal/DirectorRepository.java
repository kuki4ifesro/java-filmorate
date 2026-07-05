package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
public class DirectorRepository {

    private final JdbcTemplate jdbc;

    public DirectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public List<Director> getAllDirectors() {
        String sql = "SELECT id, name FROM directors ORDER BY id";
        return jdbc.query(sql, (rs, rowNum) ->
                new Director(rs.getLong("id"), rs.getString("name"))
        );
    }

    public Optional<Director> getDirectorById(Long id) {
        String sql = "SELECT id, name FROM directors WHERE id = ?";
        try {
            Director director = jdbc.queryForObject(sql,
                    (rs, rowNum) -> new Director(rs.getLong("id"), rs.getString("name")),
                    id);
            return Optional.ofNullable(director);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        director.setId(generatedId);
        return director;
    }

    public void update(Director director) {
        int rowsAffected = jdbc.update(
                "UPDATE directors SET name = ? WHERE id = ?",
                director.getName(),
                director.getId()
        );

        if (rowsAffected == 0) {
            throw new DirectorNotFoundException("Режиссер с ID " + director.getId() + " не найден");
        }
    }

    @Transactional
    public void delete(Long id) {
        jdbc.update("DELETE FROM film_directors WHERE director_id = ?", id);
        jdbc.update("DELETE FROM directors WHERE id = ?", id);
    }

    public List<Director> getDirectorsByIds(Set<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            return List.of();
        }

        String inClause = String.join(",", Collections.nCopies(directorIds.size(), "?"));
        String sql = "SELECT id, name FROM directors WHERE id IN (" + inClause + ")";

        List<Object> params = new ArrayList<>(directorIds);

        return jdbc.query(
                sql,
                (rs, rowNum) -> new Director(rs.getLong("id"), rs.getString("name")),
                params.toArray()
        );
    }
}
