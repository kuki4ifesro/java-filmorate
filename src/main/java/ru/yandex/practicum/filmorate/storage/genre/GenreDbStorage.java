package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, new GenreRowMapper());
    }

    public Optional<Genre> findById(Long id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, new GenreRowMapper(), id);
        return genres.isEmpty() ? Optional.empty() : Optional.of(genres.get(0));
    }

    public Map<Long, Genre> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, name FROM genres WHERE id IN (" + placeholders + ")";
        List<Genre> genres = jdbcTemplate.query(sql, new GenreRowMapper(), ids.toArray());
        return genres.stream().collect(Collectors.toMap(Genre::getId, genre -> genre));
    }

    private static class GenreRowMapper implements RowMapper<Genre> {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }
    }
}
