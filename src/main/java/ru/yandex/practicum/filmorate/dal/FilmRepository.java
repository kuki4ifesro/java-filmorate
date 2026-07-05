package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmRepository {

    private static final String FILM_SELECT_WITH_MPA = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   f.mpa_id AS mpa_id,
                   m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            """;

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    @Autowired
    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper filmRowMapper, GenreRowMapper genreRowMapper) {
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
    }

    public Film create(Film film) {
        String sqlQuery = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());

            if (film.getMpa() != null) {
                stmt.setLong(5, film.getMpa().getId());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }

            return stmt;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(generatedId);

        updateFilmGenres(generatedId, film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet()));

        updateFilmLikes(generatedId, film.getLikes());

        Set<Long> directorIds = film.getDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        validateDirectors(directorIds);
        updateFilmDirectors(generatedId, directorIds);

        return getById(generatedId);
    }

    public Film update(Film film) {
        String updateFilmQuery = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;

        int rowsAffected = jdbc.update(updateFilmQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (rowsAffected == 0) {
            throw new EntityNotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        updateFilmGenres(film.getId(), film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet()));

        Set<Long> directorIds = film.getDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        validateDirectors(directorIds);
        updateFilmDirectors(film.getId(), directorIds);

        return getById(film.getId());
    }

    private void updateFilmGenres(Long filmId, Set<Long> genreIds) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", filmId);

        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batchArgs = new TreeSet<>(genreIds).stream()
                .map(genreId -> new Object[]{filmId, genreId})
                .collect(Collectors.toList());

        jdbc.batchUpdate(sql, batchArgs);
    }

    private void updateFilmLikes(Long filmId, Set<Long> userIds) {
        jdbc.update("DELETE FROM likes WHERE film_id = ?", filmId);

        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

        List<Object[]> batchArgs = userIds.stream()
                .map(userId -> new Object[]{filmId, userId})
                .collect(Collectors.toList());

        jdbc.batchUpdate(sql, batchArgs);
    }

    private void updateFilmDirectors(Long filmId, Set<Long> directorIds) {
        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", filmId);

        if (directorIds == null || directorIds.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";

        List<Object[]> batchArgs = directorIds.stream()
                .map(directorId -> new Object[]{filmId, directorId})
                .collect(Collectors.toList());

        jdbc.batchUpdate(sql, batchArgs);
    }

    public List<Film> getAll() {
        String sql = FILM_SELECT_WITH_MPA + " ORDER BY f.id";

        List<Film> films = jdbc.query(sql, filmRowMapper);
        loadFilmRelations(films);
        return films;
    }

    public Film getById(Long id) {
        String sql = FILM_SELECT_WITH_MPA + " WHERE f.id = ?";

        Film film = jdbc.queryForObject(sql, filmRowMapper, id);
        loadFilmRelations(film);
        return film;
    }

    public boolean addLike(Long filmId, Long userId) {
        String filmCheckSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        int filmCount = jdbc.queryForObject(filmCheckSql, Integer.class, filmId);

        if (filmCount == 0) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        int count = jdbc.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count > 0) {
            return false;
        }

        String insertSql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(insertSql, filmId, userId);

        return true;
    }

    public void deleteLike(Long filmId, Long userId) {
        String filmCheckSql = "SELECT COUNT(*) FROM films WHERE id = ?";
        int filmCount = jdbc.queryForObject(filmCheckSql, Integer.class, filmId);

        if (filmCount == 0) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        String likeCheckSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        int likeCount = jdbc.queryForObject(likeCheckSql, Integer.class, filmId, userId);

        if (likeCount == 0) {
            throw new NotFoundException(
                    "Пользователь с ID " + userId + " не ставил лайк фильму с ID " + filmId
            );
        }

        String deleteSql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(deleteSql, filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        String sql = FILM_SELECT_WITH_MPA + """
                ORDER BY (
                    SELECT COUNT(*)
                    FROM likes
                    WHERE film_id = f.id
                ) DESC, f.id DESC
                LIMIT ?
                """;

        List<Film> films = jdbc.query(sql, filmRowMapper, count);
        loadFilmRelations(films);
        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = FILM_SELECT_WITH_MPA + """
                JOIN likes l ON f.id = l.film_id
                WHERE l.user_id IN (?, ?)
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                HAVING COUNT(DISTINCT l.user_id) = 2
                ORDER BY (
                    SELECT COUNT(*)
                    FROM likes l_all
                    WHERE l_all.film_id = f.id
                ) DESC, f.id
                """;

        List<Film> films = jdbc.query(sql, filmRowMapper, userId, friendId);
        loadFilmRelations(films);
        return films;
    }

    private void loadFilmRelations(Film film) {
        if (film == null) {
            return;
        }

        loadFilmRelations(List.of(film));
    }

    private void loadFilmRelations(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        loadGenresForFilms(films, filmIds);
        loadLikesForFilms(films, filmIds);
        loadDirectorsForFilms(films, filmIds);
    }

    private void loadGenresForFilms(List<Film> films, Set<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return;
        }

        String inClause = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", ", "(", ")"));

        String genreSql = """
                SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN """ + inClause + """
                ORDER BY fg.film_id, g.id
                """;

        Map<Long, List<Genre>> genreMap = new HashMap<>();

        jdbc.query(genreSql, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");

            Genre genre = new Genre();
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("genre_name"));

            genreMap.computeIfAbsent(filmId, key -> new ArrayList<>()).add(genre);

            return null;
        }, filmIds.toArray());

        for (Film film : films) {
            List<Genre> genres = genreMap.getOrDefault(film.getId(), Collections.emptyList());
            film.setGenres(new LinkedHashSet<>(genres));
        }
    }

    private void loadLikesForFilms(List<Film> films, Set<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return;
        }

        String inClause = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", ", "(", ")"));

        String likesSql = """
                SELECT film_id, user_id AS like_user_id
                FROM likes
                WHERE film_id IN """ + inClause;

        Map<Long, Set<Long>> likesMap = new HashMap<>();

        jdbc.query(likesSql, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("like_user_id");

            likesMap.computeIfAbsent(filmId, key -> new HashSet<>()).add(userId);

            return null;
        }, filmIds.toArray());

        for (Film film : films) {
            Set<Long> likes = likesMap.getOrDefault(film.getId(), Collections.emptySet());
            film.setLikes(likes);
        }
    }

    private void loadDirectorsForFilms(List<Film> films, Set<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return;
        }

        String inClause = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", ", "(", ")"));

        String directorSql = """
                SELECT fd.film_id, d.id AS director_id, d.name AS director_name
                FROM film_directors fd
                JOIN directors d ON fd.director_id = d.id
                WHERE fd.film_id IN """ + inClause + """
                ORDER BY fd.film_id, d.id
                """;

        Map<Long, Set<Director>> directorMap = new HashMap<>();

        jdbc.query(directorSql, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");

            Director director = new Director();
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("director_name"));

            directorMap.computeIfAbsent(filmId, key -> new LinkedHashSet<>()).add(director);

            return null;
        }, filmIds.toArray());

        for (Film film : films) {
            Set<Director> directors = directorMap.getOrDefault(film.getId(), Collections.emptySet());
            film.setDirectors(directors);
        }
    }

    public Set<Genre> loadGenresForFilm(Long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        try {
            List<Genre> genres = jdbc.query(sql, genreRowMapper, filmId);
            return new LinkedHashSet<>(genres);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptySet();
        }
    }

    public Set<Director> loadDirectorsForFilm(Long filmId) {
        if (filmId == null) {
            return Collections.emptySet();
        }

        String sql = """
                SELECT d.id, d.name
                FROM directors d
                JOIN film_directors fd ON d.id = fd.director_id
                WHERE fd.film_id = ?
                ORDER BY d.id
                """;

        try {
            List<Director> directors = jdbc.query(sql, (rs, rowNum) -> {
                Director director = new Director();
                director.setId(rs.getLong("id"));
                director.setName(rs.getString("name"));
                return director;
            }, filmId);

            return new LinkedHashSet<>(directors);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptySet();
        }
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String orderClause = switch (sortBy) {
            case "year" -> "EXTRACT(YEAR FROM f.release_date) ASC";
            case "likes" -> "(SELECT COUNT(*) FROM likes WHERE film_id = f.id) DESC";
            default -> throw new ValidationException("Неподдерживаемый параметр сортировки: " + sortBy);
        };

        String sql = FILM_SELECT_WITH_MPA + """
                JOIN film_directors fd ON f.id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY %s
                """.formatted(orderClause);

        List<Film> films = jdbc.query(sql, filmRowMapper, directorId);
        loadFilmRelations(films);
        return films;
    }

    public void deleteFilm(Long filmId) {
        String deleteFilm = "DELETE FROM films WHERE id = ?";
        int result = jdbc.update(deleteFilm, filmId);

        if (result <= 0) {
            log.info("Фильм {} не получилось удалить!", filmId);
            return;
        }

        log.info("Фильм с id {} удален", filmId);
    }

    public void validateDirectors(Set<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            return;
        }

        String inClause = directorIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT COUNT(*) FROM directors WHERE id IN (" + inClause + ")";

        Object[] params = directorIds.toArray();

        Integer existingCount = jdbc.queryForObject(sql, Integer.class, params);

        if (existingCount == null || existingCount != directorIds.size()) {
            throw new EntityNotFoundException("Следующие режиссеры не найдены: " + directorIds);
        }
    }

    public List<Film> getPopularFilmsByGenreAndYear(Long count, Long genreId, Long year) {
        Long actualLimit = count != null ? count : 5L;

        StringBuilder sql = new StringBuilder("""
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id AS mpa_id,
                       m.name AS mpa_name,
                       COUNT(l.user_id) AS like_count
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN likes l ON f.id = l.film_id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (year != null) {
            sql.append(" AND EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        if (genreId != null) {
            sql.append("""
                     AND EXISTS (
                        SELECT 1
                        FROM film_genres fg2
                        WHERE fg2.film_id = f.id AND fg2.genre_id = ?
                     )
                    """);
            params.add(genreId);
        }

        sql.append("""
                 GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                 ORDER BY like_count DESC, f.id
                 LIMIT ?
                """);
        params.add(actualLimit);

        List<Film> films = jdbc.query(sql.toString(), filmRowMapper, params.toArray());
        loadFilmRelations(films);
        return films;
    }

    public Set<Long> getUserLikedFilms(Long userId) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        List<Long> filmIds = jdbc.queryForList(sql, Long.class, userId);
        return new HashSet<>(filmIds);
    }

    public Map<Long, Set<Long>> getLikedFilmsByUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inClause = userIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", ", "(", ")"));

        String sql = """
                SELECT user_id, film_id
                FROM likes
                WHERE user_id IN """ + inClause;

        Map<Long, Set<Long>> result = new HashMap<>();

        jdbc.query(sql, (rs, rowNum) -> {
            Long likedUserId = rs.getLong("user_id");
            Long filmId = rs.getLong("film_id");

            result.computeIfAbsent(likedUserId, key -> new HashSet<>()).add(filmId);

            return null;
        }, userIds.toArray());

        return result;
    }

    public List<Long> findSimilarUsers(Long userId, int limit) {
        String sql = """
                SELECT l2.user_id
                FROM likes l1
                JOIN likes l2 ON l1.film_id = l2.film_id AND l1.user_id != l2.user_id
                WHERE l1.user_id = ?
                GROUP BY l2.user_id
                ORDER BY COUNT(*) DESC
                LIMIT ?
                """;

        return jdbc.queryForList(sql, Long.class, userId, limit);
    }

    public List<Film> getFilmsByIds(Set<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inClause = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", ", "(", ")"));

        String sql = FILM_SELECT_WITH_MPA + " WHERE f.id IN " + inClause;

        List<Film> films = jdbc.query(sql, filmRowMapper, filmIds.toArray());
        loadFilmRelations(films);
        return films;
    }

    public List<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector) {
        if (searchByTitle && !searchByDirector) {
            String searchFilm = FILM_SELECT_WITH_MPA + """
                    WHERE f.name ILIKE CONCAT('%', ?, '%')
                    ORDER BY (
                        SELECT COUNT(*)
                        FROM likes
                        WHERE film_id = f.id
                    ) DESC
                    """;

            List<Film> films = jdbc.query(searchFilm, filmRowMapper, query);
            loadFilmRelations(films);
            return films;
        }

        if (!searchByTitle && searchByDirector) {
            String searchFilm = FILM_SELECT_WITH_MPA + """
                    LEFT JOIN film_directors fd ON fd.film_id = f.id
                    LEFT JOIN directors d ON d.id = fd.director_id
                    WHERE d.name ILIKE CONCAT('%', ?, '%')
                    ORDER BY (
                        SELECT COUNT(*)
                        FROM likes
                        WHERE film_id = f.id
                    ) DESC
                    """;

            List<Film> films = jdbc.query(searchFilm, filmRowMapper, query);
            loadFilmRelations(films);
            return films;
        }

        String searchFilm = FILM_SELECT_WITH_MPA + """
                LEFT JOIN film_directors fd ON fd.film_id = f.id
                LEFT JOIN directors d ON d.id = fd.director_id
                WHERE d.name ILIKE CONCAT('%', ?, '%') OR f.name ILIKE CONCAT('%', ?, '%')
                ORDER BY (
                    SELECT COUNT(*)
                    FROM likes
                    WHERE film_id = f.id
                ) DESC
                """;

        List<Film> films = jdbc.query(searchFilm, filmRowMapper, query, query);
        loadFilmRelations(films);
        return films;
    }
}