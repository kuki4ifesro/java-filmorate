package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;

    public Director create(Director director) {
        validateDirector(director);
        return directorRepository.create(director);
    }

    public Director update(Director director) {
        if (director.getId() == null) {
            throw new IllegalArgumentException("ID режиссера обязателен");
        }

        // Проверка существования
        Director existingDirector = directorRepository.getDirectorById(director.getId())
                .orElseThrow(() -> new DirectorNotFoundException(
                        "Режиссер с ID " + director.getId() + " не найден"));

        validateDirector(director);

        directorRepository.update(director);
        return getById(director.getId())
                .orElseThrow(() -> new DirectorNotFoundException(
                        "Режиссер с ID " + director.getId() + " не найден после обновления"
                ));
    }

    public void delete(Long id) {
        if (!directorRepository.getDirectorById(id).isPresent()) {
            throw new DirectorNotFoundException("Режиссер с ID " + id + " не найден");
        }
        directorRepository.delete(id);
    }

    public Optional<Director> getById(Long id) {
        return directorRepository.getDirectorById(id);
    }

    public List<Director> getAllDirectors() {
        return directorRepository.getAllDirectors();
    }

    public List<Director> getDirectorsByIds(Set<Long> directorIds) {
        return directorRepository.getDirectorsByIds(directorIds);
    }

    private void validateDirector(Director director) {
        if (director == null) {
            throw new IllegalArgumentException("Объект режиссера не может быть null");
        }

        String name = director.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя режиссера не может быть пустым");
        }

        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Имя режиссера должно содержать минимум 2 символа");
        }

        if (name.replaceAll("\\s+", "").isEmpty()) {
            throw new IllegalArgumentException("Имя режиссера не может состоять только из пробелов");
        }
    }

    public boolean exists(Long directorId) {
        return directorRepository.getDirectorById(directorId).isPresent();
    }

    public long count() {
        return getAllDirectors().size();
    }
}
