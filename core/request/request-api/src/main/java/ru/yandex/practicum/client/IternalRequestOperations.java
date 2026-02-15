package ru.yandex.practicum.client;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;

import java.util.List;

public interface IternalRequestOperations {
    @GetMapping("/confirmed-counts")
    List<ConfirmedRequestCount> getConfirmedCounts(@RequestParam("eventIds") List<Long> eventIds,
                                                   @RequestParam("status") RequestStatus status);

    @GetMapping("/{userId}/{eventId}")
    List<RequestDto> getEventRequests(@PathVariable("userId") Long userId,
                                      @PathVariable("eventId") Long eventId);

    @PatchMapping("/{userId}/{eventId}")
    EventRequestStatusUpdateResult updateStatuses(@PathVariable("userId") Long userId,
                                                  @PathVariable("eventId") Long eventId,
                                                  @RequestBody EventRequestStatusUpdateRequest request);
}
