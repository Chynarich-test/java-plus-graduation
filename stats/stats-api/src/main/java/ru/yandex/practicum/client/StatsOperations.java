package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.StatsRequest;
import ru.yandex.practicum.dto.ViewStatsDto;

import java.util.List;

public interface StatsOperations {
    @PostMapping("/hit")
    ResponseEntity<EndpointHitDto> saveHit(@Valid @RequestBody EndpointHitDto hitDto);

    @GetMapping("/stats")
    List<ViewStatsDto> getStats(@Valid @SpringQueryMap StatsRequest request);
}
