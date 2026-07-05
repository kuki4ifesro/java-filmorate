package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService service;

    @PostMapping
    public ResponseEntity<FilmDto> create(@RequestBody @Valid FilmDto dto) {
        FilmDto created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<FilmDto> updateFilm(@RequestBody @Valid FilmDto dto) {
        if (dto.getId() == null) {
            throw new BadRequestException("ID фильма обязателен для обновления");
        }

        FilmDto updatedFilm = service.update(dto);
        return ResponseEntity.ok(updatedFilm);
    }

    @GetMapping
    public ResponseEntity<List<FilmDto>> getAll() {
        log.info("Запрошен вывод всех фильмов");
        List<FilmDto> films = service.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(films);
    }

    @GetMapping("/{id}")
    public FilmDto getById(@PathVariable Long id) {
        log.info("Запрошены данные фильма с ID {}", id);
        return service.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, id);
        service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с ID {} убрал лайк у фильма с ID {}", userId, id);
        service.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDto>> getPopularFilmsByGenreAndYear(
            @RequestParam(defaultValue = "10") Long count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Long year) {

        log.info("Запрошен топ-{} фильмов. Жанр: {}, Год: {}", count, genreId, year);

        List<FilmDto> popularFilmsByGenreAndYear = service.getPopularFilmsByGenreAndYear(count, genreId, year);
        return ResponseEntity.status(HttpStatus.OK).body(popularFilmsByGenreAndYear);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirector(@PathVariable Long directorId,
                                            @RequestParam(defaultValue = "likes") String sortBy) {
        log.info("Запрошены фильмы режиссера с ID {} с сортировкой по {}", directorId, sortBy);
        List<Film> films = service.getFilmsByDirector(directorId, sortBy);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(@RequestParam Long userId,
                                        @RequestParam Long friendId) {
        log.info("Запрошены общие фильмы пользователей с ID {} и {}", userId, friendId);
        return service.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable Long filmId) {
        log.info("Запрос на удаление фильма: {}", filmId);
        service.deleteFilm(filmId);
    }

    @GetMapping("/search")
    public List<FilmDto> searchFilms(@RequestParam String query,
                                  @RequestParam(defaultValue = "title,director") String by) {
        return service.searchFilms(query, by);
    }
}
