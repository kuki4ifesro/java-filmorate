package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"})
class FilmDbStorageTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private FilmDbStorage filmStorage;

	@AfterEach
	void tearDown() {
		jdbcTemplate.execute("DELETE FROM likes");
		jdbcTemplate.execute("DELETE FROM film_genres");
		jdbcTemplate.execute("DELETE FROM films");
	}

	private FilmDbStorage getFilmStorage() {
		if (filmStorage == null) {
			filmStorage = new FilmDbStorage(jdbcTemplate);
		}
		return filmStorage;
	}

	private void ensureUserExistsForLikes() {
		jdbcTemplate.update(
				"MERGE INTO users (id, email, login, name, birthday) KEY(id) VALUES (?, ?, ?, ?, ?)",
				1L,
				"like-user@example.com",
				"likeuser",
				"Like User",
				java.sql.Date.valueOf(LocalDate.of(1990, 1, 1))
		);
	}

    @Test
    public void testCreateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = getFilmStorage().create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Film");
        assertThat(created.getDescription()).isEqualTo("Test Description");
    }

    @Test
    public void testFindFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = getFilmStorage().create(film);

        Optional<Film> filmOptional = getFilmStorage().findById(created.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getId()).isEqualTo(created.getId()));
    }

    @Test
    public void testFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Test Film 1");
        film1.setDescription("Test Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        getFilmStorage().create(film1);

        Film film2 = new Film();
        film2.setName("Test Film 2");
        film2.setDescription("Test Description 2");
        film2.setReleaseDate(LocalDate.of(2005, 1, 1));
        film2.setDuration(90);
        getFilmStorage().create(film2);

        List<Film> films = getFilmStorage().findAll();

        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = getFilmStorage().create(film);

        created.setName("Updated Film");
        created.setDescription("Updated Description");
        Film updated = getFilmStorage().update(created);

        assertThat(updated.getName()).isEqualTo("Updated Film");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    public void testDeleteFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = getFilmStorage().create(film);

        getFilmStorage().delete(created.getId());

        Optional<Film> filmOptional = getFilmStorage().findById(created.getId());
        assertThat(filmOptional).isEmpty();
    }

    @Test
    public void testAddLike() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = getFilmStorage().create(film);

        ensureUserExistsForLikes();

        getFilmStorage().addLike(created.getId(), 1L);

        Optional<Film> filmOptional = getFilmStorage().findById(created.getId());
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getLikes()).contains(1L));
    }

    @Test
    public void testRemoveLike() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = getFilmStorage().create(film);

        ensureUserExistsForLikes();

        getFilmStorage().addLike(created.getId(), 1L);
        getFilmStorage().removeLike(created.getId(), 1L);

        Optional<Film> filmOptional = getFilmStorage().findById(created.getId());
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getLikes()).doesNotContain(1L));
    }

    @Test
    public void testCreateFilmWithGenres() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);

        Set<Genre> genres = new LinkedHashSet<>();
        Genre genre1 = new Genre();
        genre1.setId(1L);
        genres.add(genre1);
        Genre genre2 = new Genre();
        genre2.setId(2L);
        genres.add(genre2);
        film.setGenres(genres);

        Film created = getFilmStorage().create(film);

        Optional<Film> filmOptional = getFilmStorage().findById(created.getId());
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getGenres()).hasSize(2);
                    assertThat(f.getMpa()).isNotNull();
                });
    }
}
