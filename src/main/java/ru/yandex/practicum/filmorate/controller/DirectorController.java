package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ErrorResponse;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<List<DirectorDto>> getAllDirectors() {
        List<Director> directors = directorService.getAllDirectors();
        List<DirectorDto> dtoList = DirectorMapper.toDto(directors);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getDirectorById(@PathVariable Long id) {
        try {
            Director director = directorService.getById(id)
                    .orElseThrow(() -> new DirectorNotFoundException("Режиссёр с ID " + id + " не найден"));
            DirectorDto dto = DirectorMapper.toDto(director);
            return ResponseEntity.ok(dto);
        } catch (DirectorNotFoundException e) {
            ErrorResponse error = new ErrorResponse(e.getMessage(), 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<DirectorDto> createDirector(@Valid @RequestBody Director director) {
        Director created = directorService.create(director);
        DirectorDto dto = DirectorMapper.toDto(created);
        return ResponseEntity.status(201).body(dto);
    }

    @PutMapping
    public ResponseEntity<?> updateDirector(@RequestBody DirectorDto directorDto) {
        if (directorDto.getId() == null) {
            ErrorResponse error = new ErrorResponse("ID режиссёра обязателен для обновления", 400);
            return ResponseEntity.badRequest().body(error);
        }

        try {
            Director director = DirectorMapper.toEntity(directorDto);
            Director updatedDirector = directorService.update(director);
            DirectorDto updatedDto = DirectorMapper.toDto(updatedDirector);
            return ResponseEntity.ok(updatedDto);
        } catch (DirectorNotFoundException e) {
            ErrorResponse error = new ErrorResponse(e.getMessage(), 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse("Ошибка обновления режиссёра", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DirectorDto> updateDirector(
            @PathVariable Long id,
            @RequestBody DirectorDto directorDto) {
        Director director = DirectorMapper.toEntity(directorDto);
        director.setId(id);

        Director updatedDirector = directorService.update(director);
        return ResponseEntity.ok(DirectorMapper.toDto(updatedDirector));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDirector(@PathVariable Long id) {
        directorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}