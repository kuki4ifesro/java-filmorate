package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final FilmService filmService;

    @PostMapping
    public UserDto createUser(@Validated(User.OnCreate.class) @RequestBody UserDto dto) {
        log.info("Начато создание пользователя {}", dto);
        return service.create(dto);
    }

    @PutMapping
    public UserDto update(@Validated(User.OnUpdate.class) @RequestBody UserDto dto) throws UserNotFoundException {
        log.info("Начато обновление пользователя {}", dto);
        return service.update(dto)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с ID " + dto.getId() + " не найден",
                        dto.getId()
                ));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        log.info("Запрошен вывод всех пользователей");
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) throws UserNotFoundException {
        log.info("Запрошены данные пользователя с ID {}", id);
        return service.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) throws UserNotFoundException {
        log.info("Запрос на добавление в друзья: пользователь {} добавляет пользователя {}", id, friendId);
        service.addFriend(id, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) throws UserNotFoundException {
        log.info("Запрос на удаление из друзей: пользователь {} удаляет пользователя {}", userId, friendId);
        service.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable Long id) {
        log.info("Запрошен список друзей пользователя с ID {}", id);
        final List<UserDto> friends = service.getFriends(id);
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрошен список общих друзей для пользователей с ID {} и {}", id, otherId);
        final List<UserDto> friends = service.getCommonFriends(id, otherId);
        return friends;
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Запрос на удаление пользователя: {}", userId);
        service.deleteUser(userId);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable Long id) {
        log.info("Запрошены рекомендации для пользователя с ID {}", id);
        return filmService.getRecommendations(id);
    }

    @GetMapping("/{id}/feed")
    public List<EventDto> getFeed(@PathVariable Long id) {
        log.info("Запрошена лента событий для пользователя с ID {}", id);
        return service.getFeed(id);
    }
}
