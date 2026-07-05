package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {

    private Long id;

    private String name;

    private String description;

    private LocalDate releaseDate;

    private Long duration;

    private Set<Long> likes = new HashSet<>();

    private Mpa mpa;

    private Set<Genre> genres = new LinkedHashSet<>();

    private Set<Director> directors = new HashSet<>();

}
