package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaRepository mpaRepository;

    public List<Mpa> getAllMpa() {
        return mpaRepository.getAllMpa();
    }

    public Mpa getMpaById(Long id) {
        if (id == null || id < 1 || id > 5) {
            throw new NotFoundException("Рейтинг с ID " + id + " не найден");
        }

        return mpaRepository.getMpaById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с ID " + id + " не найден"));
    }
}
