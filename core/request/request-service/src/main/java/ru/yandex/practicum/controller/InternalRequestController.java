package ru.yandex.practicum.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalRequestController{
    private final RequestService service;

    @GetMapping("/confirmed-counts")
    public List<ConfirmedRequestCount> getConfirmedCounts(@RequestParam("eventIds") List<Long> eventIds,
                                                          @RequestParam("status") RequestStatus status) {
        return service.countConfirmedRequests(eventIds, status);
    }

    @GetMapping("/{userId}/{eventId}")
    public List<RequestDto> getEventRequests(@PathVariable("userId") Long userId,
                                             @PathVariable("eventId") Long eventId) {
        return service.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{userId}/{eventId}")
    public EventRequestStatusUpdateResult updateStatuses(@PathVariable("userId") Long userId,
                                                         @PathVariable("eventId") Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest request) {
        return service.changeRequestStatus(userId, eventId, request);
    }
}
