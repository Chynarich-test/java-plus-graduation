package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.StatsRequest;
import ru.yandex.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsOperations {
    @PostMapping("/hit")
    ResponseEntity<EndpointHitDto> saveHit(@Valid @RequestBody EndpointHitDto hitDto);

    @GetMapping("/stats")
    List<ViewStatsDto> getStats(StatsRequest request);
}
