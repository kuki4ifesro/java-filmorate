package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDto {

    private Long reviewId;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва обязателен")
    private Boolean isPositive;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

    @NotNull(message = "ID фильма обязателен")
    private Long filmId;

    private Integer useful = 0;
}