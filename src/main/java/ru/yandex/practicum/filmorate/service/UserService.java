package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.PatchValue;
import ru.yandex.practicum.filmorate.validation.PropertyValidationSupport;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	@Qualifier("userDbStorage")
	private final UserStorage userStorage;
	private final PropertyValidationSupport propertyValidation;

	public User create(User user) {
		normalizeName(user);
		User created = userStorage.create(user);
		log.info("Создан пользователь id={}, login={}", created.getId(), created.getLogin());
		return created;
	}

	public User update(User patch) {
		User existing = getRequired(patch.getId());
		mergeUser(existing, patch);
		User updated = userStorage.update(existing);
		log.info("Обновлён пользователь id={}, login={}", updated.getId(), updated.getLogin());
		return updated;
	}

	public List<User> findAll() {
		return userStorage.findAll();
	}

	public User findById(Long id) {
		return getRequired(id);
	}

	public void addFriend(Long id, Long friendId) {
		User user = getRequired(id);
		User friend = getRequired(friendId);
		user.getFriends().add(friendId);
		if (userStorage instanceof ru.yandex.practicum.filmorate.storage.user.UserDbStorage) {
			((ru.yandex.practicum.filmorate.storage.user.UserDbStorage) userStorage).addFriend(id, friendId);
		} else {
			userStorage.update(user);
		}
		log.info("Пользователь {} добавил в друзья {}", id, friendId);
	}

	public void removeFriend(Long id, Long friendId) {
		User user = getRequired(id);
		User friend = getRequired(friendId);
		user.getFriends().remove(friendId);
		if (userStorage instanceof ru.yandex.practicum.filmorate.storage.user.UserDbStorage) {
			((ru.yandex.practicum.filmorate.storage.user.UserDbStorage) userStorage).removeFriend(id, friendId);
		} else {
			userStorage.update(user);
		}
		log.info("Пользователь {} удалил из друзей {}", id, friendId);
	}

	public List<User> getFriends(Long id) {
		User user = getRequired(id);
		return user.getFriends().stream()
				.map(this::getRequired)
				.collect(Collectors.toList());
	}

	public List<User> getCommonFriends(Long id, Long otherId) {
		User user = getRequired(id);
		User other = getRequired(otherId);
		Set<Long> commonIds = user.getFriends().stream()
				.filter(other.getFriends()::contains)
				.collect(Collectors.toSet());
		return commonIds.stream()
				.map(this::getRequired)
				.collect(Collectors.toList());
	}

	private User getRequired(Long userId) {
		return userStorage.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Пользователь с id=" + userId + " не найден"));
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
		normalizeName(target);
	}

	private void normalizeName(User user) {
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
	}
}
