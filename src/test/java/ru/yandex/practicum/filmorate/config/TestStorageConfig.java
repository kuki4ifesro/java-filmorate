package ru.yandex.practicum.filmorate.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.sql.DataSource;

@TestConfiguration
public class TestStorageConfig {

	@Bean
	@Qualifier("userDbStorage")
	@Primary
	public UserStorage inMemoryUserStorage() {
		return new InMemoryUserStorage();
	}

	@Bean
	@Qualifier("filmDbStorage")
	@Primary
	public FilmStorage inMemoryFilmStorage() {
		return new InMemoryFilmStorage();
	}

	@Bean
	@Primary
	public GenreDbStorage genreDbStorage(DataSource dataSource) {
		return new GenreDbStorage(new JdbcTemplate(dataSource));
	}

	@Bean
	@Primary
	public MpaDbStorage mpaDbStorage(DataSource dataSource) {
		return new MpaDbStorage(new JdbcTemplate(dataSource));
	}
}
