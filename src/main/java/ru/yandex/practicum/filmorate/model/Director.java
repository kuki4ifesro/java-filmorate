package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director {

    private Long id;
    @NotBlank(message = "Имя режиссёра не может быть пустым")
    @Size(max = 255, message = "Имя режиссёра не может превышать 255 символов")
    private String name;

}