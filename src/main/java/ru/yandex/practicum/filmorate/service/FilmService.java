package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ResourceNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

	private final FilmStorage filmStorage;
	private final UserStorage userStorage;
	private final MpaDbStorage mpaDbStorage;
	private final GenreDbStorage genreDbStorage;

	@Autowired
	public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
					  @Qualifier("userDbStorage") UserStorage userStorage,
					  MpaDbStorage mpaDbStorage,
					  GenreDbStorage genreDbStorage) {
		this.filmStorage = filmStorage;
		this.userStorage = userStorage;
		this.mpaDbStorage = mpaDbStorage;
		this.genreDbStorage = genreDbStorage;
	}

	public Film create(Film film) {
		validateFilmReferences(film);
		Film created = filmStorage.create(film);
		log.info("Добавлен фильм id={}, name={}", created.getId(), created.getName());
		return created;
	}

	public Film update(Film patch) {
		Film existing = getRequired(patch.getId());
		mergeFilm(existing, patch);
		validateFilmReferences(existing);
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
		getRequired(filmId);
		ensureUserExists(userId);
		filmStorage.addLike(filmId, userId);
		log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
	}

	public void removeLike(Long filmId, Long userId) {
		getRequired(filmId);
		ensureUserExists(userId);
		filmStorage.removeLike(filmId, userId);
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

	private void validateFilmReferences(Film film) {
		if (film.getMpa() != null) {
			Long mpaId = film.getMpa().getId();
			if (mpaId == null) {
				throw new ValidationException("Идентификатор рейтинга должен быть указан");
			}
			mpaDbStorage.findById(mpaId)
					.orElseThrow(() -> new ResourceNotFoundException("Рейтинг с id=" + mpaId + " не найден"));
		}

		if (film.getGenres() == null || film.getGenres().isEmpty()) {
			return;
		}

		Set<Long> genreIds = film.getGenres().stream()
				.map(genre -> {
					if (genre == null || genre.getId() == null) {
						throw new ValidationException("Идентификатор жанра должен быть указан");
					}
					return genre.getId();
				})
				.collect(Collectors.toCollection(LinkedHashSet::new));

		Map<Long, Genre> genresById = genreDbStorage.findByIds(genreIds);
		Set<Genre> resolvedGenres = new LinkedHashSet<>();
		for (Long genreId : genreIds) {
			Genre resolved = genresById.get(genreId);
			if (resolved == null) {
				throw new ResourceNotFoundException("Жанр с id=" + genreId + " не найден");
			}
			resolvedGenres.add(resolved);
		}
		film.setGenres(resolvedGenres);
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
		if (patch.getMpa() != null) {
			target.setMpa(patch.getMpa());
		}
		if (patch.getGenres() != null) {
			target.setGenres(patch.getGenres());
		}
	}
}
