package ru.yandex.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
public class PublicEventController{
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> searchPublicEvents(@Valid PublicEventFilter filter) {
        return eventService.searchPublicEvents(filter);
    }

    @GetMapping("/{id}")
    public EventFullDto findPublicEventById(@PathVariable long id,
                                            @RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId) {
        return eventService.findPublicEventById(id, userId);
    }

    @GetMapping("/recommendations")
    public List<EventFullDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId) {
        return eventService.getRecommendations(userId);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@PathVariable long eventId,
                          @RequestHeader("X-EWM-USER-ID") long userId) {
        eventService.likeEvent(eventId, userId);
    }

}
