package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;
    private final MpaMapper mpaMapper;

    @GetMapping
    public ResponseEntity<List<MpaDto>> getAllMpa() {
        List<Mpa> ratings = mpaService.getAllMpa();
        List<MpaDto> dtoList = mpaMapper.toDtoList(ratings);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaDto> getMpaById(@PathVariable Long id) {
        try {
            Mpa rating = mpaService.getMpaById(id);
            MpaDto dto = mpaMapper.toDto(rating);
            return ResponseEntity.ok(dto);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build(); // Возвращает 404
        }
    }
}
