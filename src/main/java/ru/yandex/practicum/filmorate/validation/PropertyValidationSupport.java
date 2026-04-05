package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PropertyValidationSupport {

	private final Validator validator;

	public PropertyValidationSupport(Validator validator) {
		this.validator = validator;
	}

	public <T> void validatePropertyOrThrow(T bean, String propertyName, Class<?>... groups) {
		Set<ConstraintViolation<T>> violations = validator.validateProperty(bean, propertyName, groups);
		if (violations.isEmpty()) {
			return;
		}
		String message = violations.stream()
				.map(ConstraintViolation::getMessage)
				.collect(Collectors.joining("; "));
		throw new ValidationException(message);
	}
}
