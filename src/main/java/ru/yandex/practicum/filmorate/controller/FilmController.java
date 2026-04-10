package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.util.List;

@Validated
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

	private final FilmService filmService;

	@GetMapping
	public List<Film> findAll() {
		return filmService.findAll();
	}

	@GetMapping("/{id}")
	public Film findById(@PathVariable Long id) {
		return filmService.findById(id);
	}

	@PostMapping
	public Film create(@Validated(Create.class) @RequestBody Film film) {
		return filmService.create(film);
	}

	@PutMapping
	public Film update(@Validated(Update.class) @RequestBody Film patch) {
		return filmService.update(patch);
	}

	@PutMapping("/{id}/like/{userId}")
	public void addLike(@PathVariable Long id, @PathVariable Long userId) {
		filmService.addLike(id, userId);
	}

	@DeleteMapping("/{id}/like/{userId}")
	public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
		filmService.removeLike(id, userId);
	}

	@GetMapping("/popular")
	public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
		return filmService.getPopular(count);
	}
}
