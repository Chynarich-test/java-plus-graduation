package ru.yandex.practicum.event.client;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.EventShortDto;
import ru.yandex.practicum.event.dto.request.PublicEventFilter;

import java.util.List;

public interface EventPublicOperations {
    @GetMapping
    List<EventShortDto> searchPublicEvents(
            @Valid PublicEventFilter filter);

    @GetMapping("/{id}")
    EventFullDto findPublicEventById(@PathVariable long id);
}
