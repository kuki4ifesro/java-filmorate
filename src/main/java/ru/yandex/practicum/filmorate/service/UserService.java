package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.UserMapper;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EventService eventService;

    public UserService(UserRepository userRepository, EventService eventService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public UserDto create(UserDto dto) {
        log.info("Создание пользователя с логином: {}", dto.getLogin());
        if (dto.getLogin() == null || dto.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (dto.getLogin().contains(" ")) {
            throw new IllegalArgumentException("Логин не должен содержать пробелы");
        }
        User user = UserMapper.toUser(dto);
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
        User createdUser = userRepository.createUser(user);
        log.debug("Пользователь создан, ID: {}, логин: {}", createdUser.getId(), createdUser.getLogin());
        return UserMapper.toUserDto(createdUser);
    }

    public Optional<UserDto> update(UserDto dto) {
        log.info("Начато обновление пользователя {}", dto);
        Optional<User> existingUserOpt = userRepository.getById(dto.getId());
        if (existingUserOpt.isEmpty()) {
            log.warn("Попытка обновить несуществующего пользователя с ID: {}", dto.getId());
            return Optional.empty();
        }
        User existingUser = existingUserOpt.get();
        applyUserUpdate(existingUser, dto);
        try {
            User updatedUser = userRepository.update(existingUser);
            log.info("Пользователь с ID {} успешно обновлён, новый логин: {}",
                    updatedUser.getId(), updatedUser.getLogin());
            return Optional.of(UserMapper.toUserDto(updatedUser));
        } catch (ValidationException e) {
            log.error("Ошибка валидации при обновлении пользователя с ID {}", dto.getId(), e);
            throw new IllegalArgumentException("Ошибка валидации: " + e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обновлении пользователя с ID {}", dto.getId(), e);
            throw new RuntimeException("Внутренняя ошибка сервера", e);
        }
    }

    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) throws UserNotFoundException {
        User user = userRepository.getById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с ID " + id + " не найден", id));
        return UserMapper.toUserDto(user);
    }

    public void addFriend(Long userId, Long friendId) throws UserNotFoundException {
        if (userId == null || friendId == null) {
            throw new IllegalArgumentException("ID пользователей не могут быть null");
        }
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Пользователь не может добавить себя в друзья");
        }
        userRepository.getById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с ID " + userId + " не найден", userId));
        userRepository.getById(friendId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с ID " + friendId + " не найден", friendId));
        log.info("Пользователь с ID {} добавляет в друзья пользователя с ID {}", userId, friendId);
        userRepository.addFriend(userId, friendId);
        eventService.record(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    public boolean deleteFriend(Long userId, Long friendId) throws UserNotFoundException {
        log.info("Пользователь с ID {} удаляет из друзей пользователя с ID {}", userId, friendId);
        if (userId == null || friendId == null) {
            throw new IllegalArgumentException("ID пользователей не могут быть null");
        }
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Пользователь не может удалить себя из друзей");
        }
        userRepository.getById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с ID " + userId + " не найден", userId));
        userRepository.getById(friendId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с ID " + friendId + " не найден", friendId));
        boolean friendshipExisted = userRepository.deleteFriend(userId, friendId);
        if (friendshipExisted) {
            eventService.record(userId, EventType.FRIEND, Operation.REMOVE, friendId);
        }
        log.info("Друг успешно удалён: {} → {}, дружба существовала: {}", userId, friendId, friendshipExisted);
        return friendshipExisted;
    }

    public List<UserDto> getCommonFriends(Long userId1, Long userId2) {
        log.info("Поиск общих друзей для пользователей с ID: {} и {}", userId1, userId2);
        return userRepository.getCommonFriends(userId1, userId2).stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getFriends(Long id) {
        log.debug("Загружаем друзей для пользователя с ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Пользователь с ID {} не найден", id);
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден", id);
        }
        List<User> friends = userRepository.getFriends(id);
        log.info("Для пользователя {} найдено {} друзей", id, friends.size());
        return friends.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void applyUserUpdate(User existingUser, UserDto dto) {
        if (dto.getEmail() != null) {
            existingUser.setEmail(dto.getEmail());
        }
        if (dto.getLogin() != null && !dto.getLogin().trim().isEmpty()) {
            existingUser.setLogin(dto.getLogin());
        }
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            existingUser.setName(dto.getName());
        }
        if (dto.getBirthday() != null) {
            existingUser.setBirthday(dto.getBirthday());
        }
    }

    public void deleteUser(Long userId) {
        getUserById(userId);
        userRepository.deleteUser(userId);
    }

    public List<EventDto> getFeed(Long userId) {
        getUserById(userId); // проверяем существование пользователя
        return eventService.getFeed(userId);
    }
}
