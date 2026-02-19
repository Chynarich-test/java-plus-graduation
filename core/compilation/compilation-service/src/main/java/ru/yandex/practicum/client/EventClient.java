package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.event.dto.EventFullDto;

import java.util.List;

@FeignClient(name = "event-service", path = "/internal/events", contextId = "eventInternalClient")
public interface EventClient{
    @GetMapping
    List<EventFullDto> getEventsByIds(@RequestParam List<Long> ids);
}
