package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseAfterCinemaBirthValidator implements ConstraintValidator<ReleaseAfterCinemaBirth, LocalDate> {

	private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

	@Override
	public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return !value.isBefore(CINEMA_BIRTHDAY);
	}
}
