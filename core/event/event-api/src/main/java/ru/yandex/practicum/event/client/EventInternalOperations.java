package ru.yandex.practicum.event.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;

import java.util.List;

public interface EventInternalOperations {
    @GetMapping
    List<EventFullDto> getEventsByIds(@RequestParam List<Long> ids);
}
