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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.Create;
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
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

	private final PropertyValidationSupport propertyValidation;
	private final Map<Long, Film> films = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	@GetMapping
	public List<Film> findAll() {
		return new ArrayList<>(films.values());
	}

	@PostMapping
	public Film create(@Validated(Create.class) @RequestBody Film film) {
		film.setId(idGenerator.getAndIncrement());
		films.put(film.getId(), film);
		log.info("Добавлен фильм id={}, name={}", film.getId(), film.getName());
		return film;
	}

	@PutMapping
	public Film update(@Validated(Update.class) @RequestBody Film patch) {
		if (!films.containsKey(patch.getId())) {
			log.warn("Попытка обновить несуществующий фильм id={}", patch.getId());
			throw new ResourceNotFoundException("Фильм с id=" + patch.getId() + " не найден");
		}
		Film existing = films.get(patch.getId());
		mergeFilm(existing, patch);
		films.put(existing.getId(), existing);
		log.info("Обновлён фильм id={}, name={}", existing.getId(), existing.getName());
		return existing;
	}

	private void mergeFilm(Film target, Film patch) {
		if (patch.getName() != null) {
			propertyValidation.validatePropertyOrThrow(patch, "name", Create.class);
			target.setName(patch.getName());
		}
		if (patch.getDescription() != null) {
			propertyValidation.validatePropertyOrThrow(patch, "description", Create.class);
			target.setDescription(patch.getDescription());
		}
		if (patch.getReleaseDate() != null) {
			propertyValidation.validatePropertyOrThrow(patch, "releaseDate", Create.class);
			target.setReleaseDate(patch.getReleaseDate());
		}
		if (patch.getDuration() != null) {
			propertyValidation.validatePropertyOrThrow(patch, "duration", Create.class);
			target.setDuration(patch.getDuration());
		}
	}
}
