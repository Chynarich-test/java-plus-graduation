package ru.yandex.practicum.event.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.event.client.EventInternalOperations;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/internal/events")
@Slf4j
@Validated
@AllArgsConstructor
public class IternalEventController implements EventInternalOperations {
    private final EventService eventService;

    @Override
    public List<EventFullDto> getEventsByIds(List<Long> ids) {
        return eventService.findAllById(ids);
    }
}
