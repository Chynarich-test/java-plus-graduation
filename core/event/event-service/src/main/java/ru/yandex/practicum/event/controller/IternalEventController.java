package ru.yandex.practicum.event.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/internal/events")
@Slf4j
@Validated
@AllArgsConstructor
public class IternalEventController{
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsByIds(@RequestParam List<Long> ids) {
        return eventService.findAllById(ids);
    }

    @GetMapping("/{id}")
    public EventFullDto findPublicEventById(@PathVariable long id) {
        return eventService.findPublicEventById(id);
    }
}
