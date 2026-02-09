package ru.yandex.practicum.controller;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.yandex.practicum.client.StatsOperations;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.StatsRequest;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.service.StatsService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("")
public class StatsController implements StatsOperations {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    public ResponseEntity<EndpointHitDto> saveHit(@Valid @RequestBody EndpointHitDto hitDto) {
        EndpointHitDto savedHit = statsService.saveHit(hitDto);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/hit/{id}")
                .buildAndExpand(savedHit.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(savedHit);
    }

    public List<ViewStatsDto> getStats(StatsRequest request) {
        return statsService.getStats(request);
    }
}
