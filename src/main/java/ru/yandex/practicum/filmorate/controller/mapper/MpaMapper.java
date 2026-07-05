package ru.yandex.practicum.filmorate.controller.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
public class MpaMapper {
    public MpaDto toDto(Mpa mpa) {
        if (mpa == null) return null;
        MpaDto dto = new MpaDto();
        dto.setId(mpa.getId());
        dto.setName(mpa.getName());
        return dto;
    }

    public List<MpaDto> toDtoList(List<Mpa> mpaList) {
        return mpaList.stream()
                .map(this::toDto)
                .toList();
    }
}