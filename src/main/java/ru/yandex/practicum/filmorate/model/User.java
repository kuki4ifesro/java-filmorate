package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.PatchValue;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;

@Data
public class User {

	@NotNull(groups = Update.class, message = "Идентификатор пользователя должен быть указан")
	private Long id;
	@NotBlank(groups = Create.class, message = "Электронная почта не может быть пустой")
	@Email(groups = Create.class, message = "Электронная почта должна иметь корректный формат")
	private String email;
	@NotBlank(groups = Create.class, message = "Логин не может быть пустым")
	@Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы", groups = Create.class)
	private String login;
	@NotBlank(groups = PatchValue.class, message = "Имя для отображения не может быть пустым")
	private String name;
	@NotNull(groups = Create.class, message = "Дата рождения обязательна")
	@PastOrPresent(message = "Дата рождения не может быть в будущем", groups = {Create.class, Update.class})
	private LocalDate birthday;
}
