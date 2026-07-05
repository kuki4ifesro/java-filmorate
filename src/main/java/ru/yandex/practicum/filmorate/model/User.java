package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    public interface OnCreate {
    }

    public interface OnUpdate {
    }

    private Long id;

    private String email;

    private String login;

    private String name;

    private LocalDate birthday;

    private Set<Long> friendIds = new HashSet<>();
}