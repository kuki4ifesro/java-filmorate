package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmDto {

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 100, message = "Название должно быть от 1 до 100 символов")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull(message = "Дата выпуска не может быть пустой")
    // @PastOrPresent(message = "Дата выпуска не может быть в будущем")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Long duration;

    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    private Mpa mpa;

    @Builder.Default
    private Set<GenreDto> genres = new HashSet<>();

    @Builder.Default
    private Set<DirectorDto> directors = new HashSet<>();
}
