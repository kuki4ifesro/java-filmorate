package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;

    @Test
    public void testFindAllGenres() {
        List<Genre> genres = genreDbStorage.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    public void testFindGenreById() {
        Optional<Genre> genreOptional = genreDbStorage.findById(1L);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1L);
                    assertThat(genre.getName()).isNotNull();
                });
    }

    @Test
    public void testFindGenreByIdNotFound() {
        Optional<Genre> genreOptional = genreDbStorage.findById(999L);

        assertThat(genreOptional).isEmpty();
    }
}
