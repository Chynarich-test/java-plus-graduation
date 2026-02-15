package ru.yandex.practicum.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.client.IternalRequestOperations;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalRequestController implements IternalRequestOperations {
    private final RequestService service;

    @Override
    public List<ConfirmedRequestCount> getConfirmedCounts(List<Long> eventIds, RequestStatus status) {
        return service.countConfirmedRequests(eventIds, status);
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        return service.getEventRequests(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult updateStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        return service.changeRequestStatus(userId, eventId, request);
    }
}
