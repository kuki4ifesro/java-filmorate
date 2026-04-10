package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.util.List;

@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public List<User> findAll() {
		return userService.findAll();
	}

	@GetMapping("/{id}")
	public User findById(@PathVariable Long id) {
		return userService.findById(id);
	}

	@PostMapping
	public User create(@Validated(Create.class) @RequestBody User user) {
		return userService.create(user);
	}

	@PutMapping
	public User update(@Validated(Update.class) @RequestBody User patch) {
		return userService.update(patch);
	}

	@PutMapping("/{id}/friends/{friendId}")
	public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
		userService.addFriend(id, friendId);
	}

	@DeleteMapping("/{id}/friends/{friendId}")
	public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
		userService.removeFriend(id, friendId);
	}

	@GetMapping("/{id}/friends")
	public List<User> getFriends(@PathVariable Long id) {
		return userService.getFriends(id);
	}

	@GetMapping("/{id}/friends/common/{otherId}")
	public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
		return userService.getCommonFriends(id, otherId);
	}
}
