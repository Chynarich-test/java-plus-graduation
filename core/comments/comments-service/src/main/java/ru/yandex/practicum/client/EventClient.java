package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;
import ru.yandex.practicum.event.dto.EventFullDto;

@FeignClient(name = "event-service", path = "/internal/events", configuration = FeignClientConfiguration.class)
public interface EventClient {
    @GetMapping("/{id}")
    EventFullDto findPublicEventById(@PathVariable long id);
}
