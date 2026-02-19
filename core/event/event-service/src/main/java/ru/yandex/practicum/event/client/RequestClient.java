package ru.yandex.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;
import ru.yandex.practicum.dto.*;

import java.util.List;

@FeignClient(name = "request-service", path = "/internal/requests", configuration = FeignClientConfiguration.class)
public interface RequestClient{
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
