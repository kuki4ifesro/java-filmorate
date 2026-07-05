package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Repository
public class GenreRepository {

    private final JdbcTemplate jdbc;

    public GenreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public Set<Genre> getAllGenres() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        List<Genre> genres = jdbc.query(sql, (rs, rowNum) ->
                new Genre(rs.getLong("id"), rs.getString("name"))
        );
        return new LinkedHashSet<>(genres);
    }

    public Optional<Genre> getGenreById(Long id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        try {
            Genre genre = jdbc.queryForObject(sql,
                    (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")),
                    id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Genre> getGenresByIds(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }

        String inClause = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String sql = "SELECT id, name FROM genres WHERE id IN (" + inClause + ")";

        List<Object> params = new ArrayList<>(genreIds);

        return jdbc.query(
                sql,
                (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")),
                params.toArray()
        );
    }
}