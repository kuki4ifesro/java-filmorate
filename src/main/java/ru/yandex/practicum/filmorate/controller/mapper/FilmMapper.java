package ru.yandex.practicum.filmorate.controller.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static FilmDto mapToFilmDto(Film film) {
        if (film == null) {
            return null;
        }

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setMpa(film.getMpa());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            dto.setGenres(GenreMapper.toDto(film.getGenres()));
        } else {
            dto.setGenres(Collections.emptySet());
        }
        dto.setLikes(film.getLikes());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<DirectorDto> directorDtos = film.getDirectors().stream()
                    .map(DirectorMapper::toDto)
                    .collect(Collectors.toSet());
            dto.setDirectors(directorDtos);
        } else {
            dto.setDirectors(Collections.emptySet());
        }

        return dto;
    }

    public static Film toFilm(FilmDto dto, Mpa mpa) {
        if (dto == null) {
            return null;
        }

        Film film = new Film();

        if (dto.getId() != null) {
            film.setId(dto.getId());
        }
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        film.setMpa(mpa);
        film.setGenres(GenreMapper.toGenres(dto.getGenres()));
        film.setLikes(dto.getLikes());
        if (dto.getDirectors() != null && !dto.getDirectors().isEmpty()) {
            Set<Director> directors = dto.getDirectors().stream()
                    .map(DirectorMapper::toEntity)
                    .collect(Collectors.toSet());
            film.setDirectors(directors);
        } else {
            film.setDirectors(Collections.emptySet());
        }

        return film;
    }
}