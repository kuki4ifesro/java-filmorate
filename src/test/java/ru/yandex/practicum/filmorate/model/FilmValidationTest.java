package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FilmValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void validFilm_hasNoViolations() {
		Film film = validFilm();
		Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
		assertThat(violations).isEmpty();
	}

	@Test
	void blankName_isInvalid() {
		Film film = validFilm();
		film.setName("   ");
		assertViolation(film, "name");
	}

	@Test
	void nullName_isInvalid() {
		Film film = validFilm();
		film.setName(null);
		assertViolation(film, "name");
	}

	@Test
	void descriptionExactly200_isValid() {
		Film film = validFilm();
		film.setDescription("a".repeat(200));
		assertThat(validator.validate(film, Create.class)).isEmpty();
	}

	@Test
	void description201Chars_isInvalid() {
		Film film = validFilm();
		film.setDescription("a".repeat(201));
		assertViolation(film, "description");
	}

	@Test
	void releaseDateBeforeCinemaBirthday_isInvalid() {
		Film film = validFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 27));
		assertViolation(film, "releaseDate");
	}

	@Test
	void releaseDateOnCinemaBirthday_isValid() {
		Film film = validFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		assertThat(validator.validate(film, Create.class)).isEmpty();
	}

	@Test
	void zeroDuration_isInvalid() {
		Film film = validFilm();
		film.setDuration(0);
		assertViolation(film, "duration");
	}

	@Test
	void negativeDuration_isInvalid() {
		Film film = validFilm();
		film.setDuration(-1);
		assertViolation(film, "duration");
	}

	@Test
	void nullDuration_isInvalid() {
		Film film = validFilm();
		film.setDuration(null);
		assertViolation(film, "duration");
	}

	@Test
	void updateWithOnlyId_hasNoViolations() {
		Film film = new Film();
		film.setId(1L);
		Set<ConstraintViolation<Film>> violations = validator.validate(film, Update.class);
		assertThat(violations).isEmpty();
	}

	@Test
	void blankNameOnUpdate_isInvalid() {
		Film film = new Film();
		film.setId(1L);
		film.setName("   ");
		assertThat(validator.validate(film, Update.class))
				.anyMatch(v -> v.getPropertyPath().toString().equals("name"));
	}

	private static Film validFilm() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Desc");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(90);
		return film;
	}

	private void assertViolation(Film film, String field) {
		assertThat(validator.validate(film, Create.class))
				.anyMatch(v -> v.getPropertyPath().toString().equals(field));
	}
}
