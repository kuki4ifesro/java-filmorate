package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void validUser_hasNoViolations() {
		User user = validUser();
		assertThat(validator.validate(user)).isEmpty();
	}

	@Test
	void blankEmail_isInvalid() {
		User user = validUser();
		user.setEmail(" ");
		assertViolation(user, "email");
	}

	@Test
	void emailWithoutAt_isInvalid() {
		User user = validUser();
		user.setEmail("not-an-email");
		assertViolation(user, "email");
	}

	@Test
	void malformedEmail_isInvalid() {
		User user = validUser();
		user.setEmail("это-неправильный?эмейл@.");
		assertViolation(user, "email");
	}

	@Test
	void blankLogin_isInvalid() {
		User user = validUser();
		user.setLogin(" ");
		assertViolation(user, "login");
	}

	@Test
	void loginWithSpaces_isInvalid() {
		User user = validUser();
		user.setLogin("bad login");
		assertViolation(user, "login");
	}

	@Test
	void blankDisplayName_isValid() {
		User user = validUser();
		user.setName("   ");
		assertThat(validator.validate(user)).isEmpty();
	}

	@Test
	void futureBirthday_isInvalid() {
		User user = validUser();
		user.setBirthday(LocalDate.now().plusDays(1));
		assertViolation(user, "birthday");
	}

	@Test
	void todayBirthday_isValid() {
		User user = validUser();
		user.setBirthday(LocalDate.now());
		assertThat(validator.validate(user)).isEmpty();
	}

	private static User validUser() {
		User user = new User();
		user.setEmail("user@yandex.ru");
		user.setLogin("login");
		user.setName("Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		return user;
	}

	private void assertViolation(User user, String field) {
		Set<ConstraintViolation<User>> violations = validator.validate(user);
		assertThat(violations)
				.anyMatch(v -> v.getPropertyPath().toString().equals(field));
	}
}
