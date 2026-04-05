package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.PatchValue;
import ru.yandex.practicum.filmorate.validation.PropertyValidationSupport;
import ru.yandex.practicum.filmorate.validation.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final PropertyValidationSupport propertyValidation;
	private final Map<Long, User> users = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	@GetMapping
	public List<User> findAll() {
		return new ArrayList<>(users.values());
	}

	@PostMapping
	public User create(@Validated(Create.class) @RequestBody User user) {
		normalizeName(user);
		user.setId(idGenerator.getAndIncrement());
		users.put(user.getId(), user);
		log.info("Создан пользователь id={}, login={}", user.getId(), user.getLogin());
		return user;
	}

	@PutMapping
	public User update(@Validated(Update.class) @RequestBody User patch) {
		if (!users.containsKey(patch.getId())) {
			log.warn("Попытка обновить несуществующего пользователя id={}", patch.getId());
			throw new ResourceNotFoundException("Пользователь с id=" + patch.getId() + " не найден");
		}
		User existing = users.get(patch.getId());
		mergeUser(existing, patch);
		users.put(existing.getId(), existing);
		log.info("Обновлён пользователь id={}, login={}", existing.getId(), existing.getLogin());
		return existing;
	}

	private void mergeUser(User target, User patch) {
		if (patch.getEmail() != null) {
			propertyValidation.validatePropertyOrThrow(patch, "email", Create.class);
			target.setEmail(patch.getEmail());
		}
		if (patch.getLogin() != null) {
			propertyValidation.validatePropertyOrThrow(patch, "login", Create.class);
			target.setLogin(patch.getLogin());
		}
		if (patch.getName() != null) {
			propertyValidation.validatePropertyOrThrow(patch, "name", PatchValue.class);
			target.setName(patch.getName());
		}
		if (patch.getBirthday() != null) {
			propertyValidation.validatePropertyOrThrow(patch, "birthday", Create.class);
			target.setBirthday(patch.getBirthday());
		}
	}

	private void normalizeName(User user) {
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
	}
}
