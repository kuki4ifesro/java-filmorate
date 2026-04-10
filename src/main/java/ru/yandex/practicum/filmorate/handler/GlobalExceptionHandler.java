package ru.yandex.practicum.filmorate.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.dto.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidationException(ValidationException e) {
		log.warn("Ошибка валидации: {}", e.getMessage());
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleResourceNotFound(ResourceNotFoundException e) {
		log.warn("Сущность не найдена: {}", e.getMessage());
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.collect(Collectors.joining("; "));
		log.warn("Ошибка валидации запроса: {}", message);
		return new ErrorResponse(message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleNotReadable(HttpMessageNotReadableException e) {
		log.warn("Тело запроса отсутствует или некорректно: {}", e.getMessage());
		return new ErrorResponse("Тело запроса отсутствует или имеет неверный формат");
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleUnhandled(Exception e) {
		log.error("Внутренняя ошибка сервера", e);
		return new ErrorResponse("Произошла внутренняя ошибка сервера");
	}
}
