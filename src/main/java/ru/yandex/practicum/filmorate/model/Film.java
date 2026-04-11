package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.ReleaseAfterCinemaBirth;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

	@NotNull(groups = Update.class, message = "Идентификатор фильма должен быть указан")
	private Long id;
	@NotBlank(groups = Create.class, message = "Название фильма не может быть пустым")
	@Pattern(regexp = ".*\\S.*", groups = Update.class, message = "Название фильма не может быть пустым")
	private String name;
	@Size(max = 200, message = "Описание не должно превышать 200 символов", groups = {Create.class, Update.class})
	private String description;
	@NotNull(groups = Create.class, message = "Дата релиза обязательна")
	@ReleaseAfterCinemaBirth(groups = {Create.class, Update.class})
	private LocalDate releaseDate;
	@NotNull(groups = Create.class, message = "Продолжительность обязательна")
	@Positive(message = "Продолжительность фильма должна быть положительным числом", groups = {Create.class, Update.class})
	private Integer duration;
	private Set<Long> likes = new HashSet<>();
}
