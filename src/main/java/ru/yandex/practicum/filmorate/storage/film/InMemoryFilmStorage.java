package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Qualifier("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

	private final Map<Long, Film> films = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	@Override
	public Film create(Film film) {
		film.setId(idGenerator.getAndIncrement());
		films.put(film.getId(), film);
		return film;
	}

	@Override
	public Film update(Film film) {
		films.put(film.getId(), film);
		return film;
	}

	@Override
	public void delete(Long filmId) {
		films.remove(filmId);
	}

	@Override
	public List<Film> findAll() {
		return new ArrayList<>(films.values());
	}

	@Override
	public Optional<Film> findById(Long filmId) {
		return Optional.ofNullable(films.get(filmId));
	}

	@Override
	public void addLike(Long filmId, Long userId) {
		Film film = films.get(filmId);
		if (film != null) {
			film.getLikes().add(userId);
		}
	}

	@Override
	public void removeLike(Long filmId, Long userId) {
		Film film = films.get(filmId);
		if (film != null) {
			film.getLikes().remove(userId);
		}
	}
}
