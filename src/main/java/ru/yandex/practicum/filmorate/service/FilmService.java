package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final MpaRepository mpaRepository;
    private final EventService eventService;

    public FilmDto create(FilmDto dto) throws ValidationException {
        validateFilmDto(dto);

        Mpa mpa = mpaRepository.getMpaById(dto.getMpa().getId())
                .orElseThrow(() -> new NotFoundException(
                        "MPA с ID " + dto.getMpa().getId() + " не существует"));

        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            Set<Long> genreIds = dto.getGenres().stream()
                    .map(GenreDto::getId)
                    .collect(Collectors.toSet());

            List<Genre> existingGenres = genreRepository.getGenresByIds(genreIds);
            if (existingGenres.size() != genreIds.size()) {
                throw new NotFoundException("Один или несколько жанров с ID " + genreIds + " не существуют");
            }
        }

        Film film = FilmMapper.toFilm(dto, mpa);
        return FilmMapper.mapToFilmDto(filmRepository.create(film));
    }

    private void validateFilmDto(FilmDto dto) {
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        LocalDate now = LocalDate.now();

        if (dto.getReleaseDate() != null) {
            if (dto.getReleaseDate().isBefore(minReleaseDate)) {
                throw new ValidationException("Дата выпуска не может быть раньше " + minReleaseDate);
            }
        }
        if (dto.getDuration() != null && dto.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    public FilmDto update(FilmDto dto) {
        validateFilmDto(dto);

        Long filmId = dto.getId();
        Film film = filmRepository.getById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        Mpa mpa = mpaRepository.getMpaById(dto.getMpa().getId())
                .orElseThrow(() -> new NotFoundException(
                        "MPA с ID " + dto.getMpa().getId() + " не существует"));

        if (dto.getGenres() != null && !dto.getGenres().isEmpty()) {
            Set<Long> genreIds = dto.getGenres().stream()
                    .map(GenreDto::getId)
                    .collect(Collectors.toSet());
            List<Genre> existingGenres = genreRepository.getGenresByIds(genreIds);
            if (existingGenres.size() != genreIds.size()) {
                throw new NotFoundException("Один или несколько жанров с ID " + genreIds + " не существуют");
            }
        }

        Film updatedFilm = filmRepository.update(FilmMapper.toFilm(dto, mpa));
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public List<FilmDto> getAll() {
        log.info("Получение всех фильмов");
        return filmRepository.getAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getById(Long id) {
        log.info("Поиск фильма с ID: {}", id);
        Film film = filmRepository.getById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }

        Set<Genre> genres = loadGenresForFilm(id);
        film.setGenres(genres);

        Set<Director> directors = loadDirectorsForFilm(id);
        film.setDirectors(directors);

        return FilmMapper.mapToFilmDto(film);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);

        userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        filmRepository.addLike(filmId, userId);
        eventService.record(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        log.info("Пользователь с ID {} убрал лайк у фильма с ID {}", userId, filmId);

        filmRepository.deleteLike(filmId, userId);
        eventService.record(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        log.info("Получение топ-{} популярных фильмов", count);
        return filmRepository.getPopularFilms(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getCommonFilms(Long userId, Long friendId) {
        log.info("Получение общих фильмов пользователей с ID {} и {}", userId, friendId);

        userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        userRepository.getById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден"));

        return filmRepository.getCommonFilms(userId, friendId).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    private Set<Genre> loadGenresForFilm(Long filmId) {
        log.info("Загрузка жанров для фильма ID {} ", filmId);
        Set<Genre> genres = filmRepository.loadGenresForFilm(filmId);
        return genres != null ? genres : new HashSet<>();
    }

    private Set<Director> loadDirectorsForFilm(Long filmId) {
        log.info("Загрузка режиссёров для фильма ID {}", filmId);
        Set<Director> directors = filmRepository.loadDirectorsForFilm(filmId);
        return directors != null ? directors : new HashSet<>();
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        Set<Long> directorIds = Set.of(directorId);
        filmRepository.validateDirectors(directorIds);
        return filmRepository.getFilmsByDirector(directorId, sortBy);
    }

    public List<FilmDto> getPopularFilmsByGenreAndYear(Long count, Long genreId, Long year) {
        log.info("Получение топ-{} популярных фильмов по жанру и году", count);

        if (count <= 0) {
            throw new IllegalArgumentException("Количество фильмов должно быть положительным");
        }
        if (year != null && (year < 1895 || year > LocalDate.now().getYear())) {
            throw new IllegalArgumentException("Год должен быть от 1895 до текущего");
        }

        List<Film> films = filmRepository.getPopularFilmsByGenreAndYear(count, genreId, year);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public void deleteFilm(Long filmId) {
        getById(filmId); //проверка на наличие фильма
        filmRepository.deleteFilm(filmId);
    }

    public List<FilmDto> getRecommendations(Long userId) {
        log.info("Запрошены рекомендации для пользователя с ID {}", userId);

        userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Set<Long> userLikedFilms = filmRepository.getUserLikedFilms(userId);

        List<Long> similarUserIds = filmRepository.findSimilarUsers(userId, 10);

        Map<Long, Integer> filmFrequencyMap = new HashMap<>();

        Map<Long, Set<Long>> similarUsersLikedFilms = filmRepository.getLikedFilmsByUsers(similarUserIds);

        for (Set<Long> similarUserLikedFilms : similarUsersLikedFilms.values()) {
            Set<Long> filmsLikedBySimilarButNotByUser = new HashSet<>(similarUserLikedFilms);
            filmsLikedBySimilarButNotByUser.removeAll(userLikedFilms);

            for (Long filmId : filmsLikedBySimilarButNotByUser) {
                filmFrequencyMap.put(filmId, filmFrequencyMap.getOrDefault(filmId, 0) + 1);
            }
        }

        int frequencyThreshold = 1;
        List<Long> sortedRecommendedFilmIds = filmFrequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= frequencyThreshold)
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());

        List<Film> recommendedFilms = filmRepository.getFilmsByIds(new HashSet<>(sortedRecommendedFilmIds));

        return recommendedFilms.stream()
                .sorted((f1, f2) -> {
                    int index1 = sortedRecommendedFilmIds.indexOf(f1.getId());
                    int index2 = sortedRecommendedFilmIds.indexOf(f2.getId());
                    return Integer.compare(index1, index2);
                })
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> searchFilms(String query, String by) {

        // парсим требование по поиску. Если нет четких инструкций - ищем по двум полям
        boolean isCorrectBy = !by.isEmpty() && !by.contains("title") && !by.contains("director");
        boolean searchByTitle = by.contains("title") || isCorrectBy;
        boolean searchByDirector = by.contains("director") || isCorrectBy;

        log.info("Поиск списка фильмов. query: {}, поиск по [названию: {}, режиссеру: {}]",
                query, searchByTitle, searchByDirector);

        return filmRepository.searchFilms(query, searchByTitle, searchByDirector).stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }


}