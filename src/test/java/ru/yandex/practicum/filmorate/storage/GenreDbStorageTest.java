package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"})
class GenreDbStorageTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private GenreDbStorage genreDbStorage;

	private GenreDbStorage getGenreStorage() {
		if (genreDbStorage == null) {
			genreDbStorage = new GenreDbStorage(jdbcTemplate);
		}
		return genreDbStorage;
	}

    @Test
    public void testFindAllGenres() {
        List<Genre> genres = getGenreStorage().findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    public void testFindGenreById() {
        Optional<Genre> genreOptional = getGenreStorage().findById(1L);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1L);
                    assertThat(genre.getName()).isNotNull();
                });
    }

    @Test
    public void testFindGenreByIdNotFound() {
        Optional<Genre> genreOptional = getGenreStorage().findById(999L);

        assertThat(genreOptional).isEmpty();
    }
}
