package ru.yandex.practicum.event.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.RequestDto;
import ru.yandex.practicum.event.client.EventPrivateOperations;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.NewEventDto;
import ru.yandex.practicum.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.event.dto.request.UserEventsQuery;
import ru.yandex.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@Slf4j
@Validated
@AllArgsConstructor
public class PrivateEventController implements EventPrivateOperations {
    private final EventService eventService;

    @Override
    public List<EventShortDto> findUserEvents(long userId,
                                              Integer from,
                                              Integer size) {
        UserEventsQuery query = new UserEventsQuery(userId, from, size);
        return eventService.findEvents(query);
    }

    @Override
    public EventFullDto createEvent(long userId,
                                    NewEventDto eventDto) {
        return eventService.createEvent(userId, eventDto);
    }

    @Override
    public EventFullDto findUserEventById(long userId,
                                          long eventId) {
        return eventService.findUserEventById(userId, eventId);
    }

    @Override
    public EventFullDto updateUserEvent(
            Long userId,
            Long eventId,
            UpdateEventUserRequest updateRequest) {
        return eventService.updateUserEvent(userId, eventId, updateRequest);
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId,
                                             Long eventId) {
        return eventService.getEventRequests(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult updateStatuses(Long userId,
                                                         Long eventId,
                                                         EventRequestStatusUpdateRequest request) {
        return eventService.changeRequestStatus(userId, eventId, request);
    }
}
