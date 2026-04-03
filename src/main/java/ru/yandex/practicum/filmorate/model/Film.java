package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ReleaseAfterCinemaBirth;

import java.time.LocalDate;

@Data
public class Film {

	private Long id;
	@NotBlank(message = "Название фильма не может быть пустым")
	private String name;
	@Size(max = 200, message = "Описание не должно превышать 200 символов")
	private String description;
	@NotNull(message = "Дата релиза обязательна")
	@ReleaseAfterCinemaBirth
	private LocalDate releaseDate;
	@NotNull(message = "Продолжительность обязательна")
	@Positive(message = "Продолжительность фильма должна быть положительным числом")
	private Integer duration;
}
