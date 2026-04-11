package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

	private final FilmStorage filmStorage;
	private final UserStorage userStorage;

	public Film create(Film film) {
		Film created = filmStorage.create(film);
		log.info("Добавлен фильм id={}, name={}", created.getId(), created.getName());
		return created;
	}

	public Film update(Film patch) {
		Film existing = getRequired(patch.getId());
		mergeFilm(existing, patch);
		Film updated = filmStorage.update(existing);
		log.info("Обновлён фильм id={}, name={}", updated.getId(), updated.getName());
		return updated;
	}

	public List<Film> findAll() {
		return filmStorage.findAll();
	}

	public Film findById(Long id) {
		return getRequired(id);
	}

	public void addLike(Long filmId, Long userId) {
		Film film = getRequired(filmId);
		ensureUserExists(userId);
		film.getLikes().add(userId);
		filmStorage.update(film);
		log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
	}

	public void removeLike(Long filmId, Long userId) {
		Film film = getRequired(filmId);
		ensureUserExists(userId);
		film.getLikes().remove(userId);
		filmStorage.update(film);
		log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
	}

	public List<Film> getPopular(int count) {
		if (count < 1) {
			throw new ValidationException("Параметр count должен быть положительным");
		}
		return filmStorage.findAll().stream()
				.sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
				.limit(count)
				.toList();
	}

	private Film getRequired(Long filmId) {
		return filmStorage.findById(filmId)
				.orElseThrow(() -> new ResourceNotFoundException("Фильм с id=" + filmId + " не найден"));
	}

	private void ensureUserExists(Long userId) {
		userStorage.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Пользователь с id=" + userId + " не найден"));
	}

	private void mergeFilm(Film target, Film patch) {
		if (patch.getName() != null) {
			target.setName(patch.getName());
		}
		if (patch.getDescription() != null) {
			target.setDescription(patch.getDescription());
		}
		if (patch.getReleaseDate() != null) {
			target.setReleaseDate(patch.getReleaseDate());
		}
		if (patch.getDuration() != null) {
			target.setDuration(patch.getDuration());
		}
	}
}
