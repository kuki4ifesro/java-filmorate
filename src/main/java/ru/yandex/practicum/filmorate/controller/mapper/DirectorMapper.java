package ru.yandex.practicum.filmorate.controller.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectorMapper {

    public static DirectorDto toDto(Director director) {
        if (director == null) return null;
        DirectorDto dto = new DirectorDto();
        dto.setId(director.getId());
        dto.setName(director.getName());
        return dto;
    }

    public static Director toEntity(DirectorDto dto) {
        if (dto == null) return null;
        Director director = new Director();
        director.setId(dto.getId());
        director.setName(dto.getName());
        return director;
    }

    // List<Director> в List<DirectorDto>
    public static List<DirectorDto> toDto(List<Director> directors) {
        if (directors == null) {
            return Collections.emptyList();
        }
        return directors.stream()
                .map(DirectorMapper::toDto)
                .collect(Collectors.toList());
    }

    // Set<Director> в List<DirectorDto>
    public static List<DirectorDto> toDto(Set<Director> directors) {
        if (directors == null) {
            return Collections.emptyList();
        }
        return directors.stream()
                .map(DirectorMapper::toDto)
                .collect(Collectors.toList());
    }

    // List<DirectorDto> в List<Director>
    public static List<Director> toDirectors(List<DirectorDto> directorDtos) {
        if (directorDtos == null) {
            return Collections.emptyList();
        }
        return directorDtos.stream()
                .map(dto -> {
                    Director director = new Director();
                    director.setId(dto.getId());
                    director.setName(dto.getName());
                    return director;
                })
                .collect(Collectors.toList());
    }

    // List<DirectorDto> в Set<Director>
    public static Set<Director> toDirectorsSet(List<DirectorDto> directorDtos) {
        if (directorDtos == null) {
            return Collections.emptySet();
        }
        return directorDtos.stream()
                .map(dto -> {
                    Director director = new Director();
                    director.setId(dto.getId());
                    director.setName(dto.getName());
                    return director;
                })
                .collect(Collectors.toSet());
    }
}