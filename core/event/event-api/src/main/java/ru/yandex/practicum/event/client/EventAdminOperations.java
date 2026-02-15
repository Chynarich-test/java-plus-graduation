package ru.yandex.practicum.event.client;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.dto.request.AdminEventFilter;

import java.util.List;

public interface EventAdminOperations {
    @GetMapping
    List<EventFullDto> searchEventsByAdmin(
            @Valid AdminEventFilter filter);

    @PatchMapping("/{eventId}")
    EventFullDto moderateEvent(
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest adminRequest);
}
