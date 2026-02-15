package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.event.client.EventPublicOperations;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.request.PublicEventFilter;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/events")
@Slf4j
@Validated
@AllArgsConstructor
public class PublicEventController implements EventPublicOperations {
    private final EventService eventService;

    @Override
    public List<EventShortDto> searchPublicEvents(PublicEventFilter filter) {
        return eventService.searchPublicEvents(filter);
    }

    @Override
    public EventFullDto findPublicEventById(long id) {
        return eventService.findPublicEventById(id);
    }
}
