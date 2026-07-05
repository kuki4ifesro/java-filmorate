package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.DataAccessException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;

@Component
@Slf4j
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        log.debug("Начинаем маппинг фильма, строка №{}", rowNum);

        Film film = new Film();
        film.setId(resultSet.getLong("id"));

        String name = resultSet.getString("name");
        if (name == null || name.trim().isEmpty()) {
            throw new DataAccessException("Поле 'name' не может быть пустым для фильма ID=" + film.getId());
        }
        film.setName(name);

        film.setDescription(resultSet.getString("description"));

        java.sql.Date sqlDate = resultSet.getDate("release_date");
        film.setReleaseDate(sqlDate != null ? sqlDate.toLocalDate() : null);

        long duration = resultSet.getLong("duration");
        film.setDuration(resultSet.wasNull() ? 0L : duration);

        Long mpaId = resultSet.getLong("mpa_id");
        if (!resultSet.wasNull()) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(getNullableString(resultSet, "mpa_name"));
            film.setMpa(mpa);
        }

        film.setLikes(new HashSet<>());
        film.setGenres(new LinkedHashSet<>());
        film.setDirectors(new LinkedHashSet<>());

        log.debug("Завершили маппинг фильма ID={}", film.getId());
        return film;
    }

    private String getNullableString(ResultSet resultSet, String columnName) {
        try {
            return resultSet.getString(columnName);
        } catch (SQLException e) {
            return null;
        }
    }
}