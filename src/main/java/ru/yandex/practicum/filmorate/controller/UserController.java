package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

	private final Map<Long, User> users = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	@GetMapping
	public List<User> findAll() {
		return new ArrayList<>(users.values());
	}

	@PostMapping
	public User create(@Valid @RequestBody User user) {
		normalizeName(user);
		user.setId(idGenerator.getAndIncrement());
		users.put(user.getId(), user);
		log.info("Создан пользователь id={}, login={}", user.getId(), user.getLogin());
		return user;
	}

	@PutMapping
	public User update(@Valid @RequestBody User user) {
		normalizeName(user);
		if (user.getId() == null) {
			log.warn("Обновление пользователя: не указан id");
			throw new ValidationException("Идентификатор пользователя должен быть указан");
		}
		if (!users.containsKey(user.getId())) {
			log.warn("Попытка обновить несуществующего пользователя id={}", user.getId());
			throw new ResourceNotFoundException("Пользователь с id=" + user.getId() + " не найден");
		}
		users.put(user.getId(), user);
		log.info("Обновлён пользователь id={}, login={}", user.getId(), user.getLogin());
		return user;
	}

	private void normalizeName(User user) {
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
	}
}
