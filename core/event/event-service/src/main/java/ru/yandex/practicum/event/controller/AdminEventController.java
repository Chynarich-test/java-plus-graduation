package ru.yandex.practicum.event.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.event.client.EventAdminOperations;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.dto.request.AdminEventFilter;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@Slf4j
@Validated
@AllArgsConstructor
public class AdminEventController implements EventAdminOperations {
    private final EventService eventService;

    @Override
    public List<EventFullDto> searchEventsByAdmin(
            AdminEventFilter filter) {
        return eventService.searchEventsByAdmin(filter);
    }

    @Override
    public EventFullDto moderateEvent(
            Long eventId,
            UpdateEventAdminRequest adminRequest) {
        return eventService.moderateEvent(eventId, adminRequest);
    }
}
