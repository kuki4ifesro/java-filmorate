package ru.yandex.practicum.filmorate.controller.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenreMapper {

    // Genre -> GenreDto (для одного объекта)
    public static GenreDto toDto(Genre genre) {
        if (genre == null) return null;
        GenreDto dto = new GenreDto();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        return dto;
    }

    // Set<Genre> -> Set<GenreDto>
    public static Set<GenreDto> toDto(Set<Genre> genres) {
        if (genres == null) {
            return Collections.emptySet();
        }

        return genres.stream()
                .sorted(Comparator.comparing(Genre::getId))
                .map(GenreMapper::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Set<GenreDto> -> Set<Genre>
    public static Set<Genre> toGenres(Set<GenreDto> genreDtos) {
        if (genreDtos == null) {
            return Collections.emptySet();
        }
        return genreDtos.stream()
                .sorted(Comparator.comparing(GenreDto::getId))
                .map(dto -> {
                    Genre genre = new Genre();
                    genre.setId(dto.getId());
                    genre.setName(dto.getName());
                    return genre;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}