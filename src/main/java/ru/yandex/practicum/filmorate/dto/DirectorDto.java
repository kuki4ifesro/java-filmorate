package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DirectorDto {

    @NotNull(message = "ID не может быть null при обновлении")
    private Long id;

    @NotBlank(message = "Имя режиссера не может быть пустым")
    @Size(max = 255, message = "Имя режиссера не может превышать 255 символов")
    private String name;

}