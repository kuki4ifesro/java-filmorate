package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventRepository;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<EventDto> getFeed(Long userId) {
        log.info("Запрошена лента событий для пользователя с ID {}", userId);
        return eventRepository.getFeedByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void record(Long userId, EventType eventType, Operation operation, Long entityId) {
        eventRepository.addEvent(userId, eventType, operation, entityId);
    }

    private EventDto toDto(Event event) {
        return EventDto.builder()
                .eventId(event.getEventId())
                .timestamp(event.getTimestamp())
                .userId(event.getUserId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .entityId(event.getEntityId())
                .build();
    }
}
