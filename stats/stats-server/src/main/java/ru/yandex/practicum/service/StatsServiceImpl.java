package ru.yandex.practicum.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.EndpointHitDto;
import ru.yandex.practicum.dto.StatsRequest;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.mapper.HitMapper;
import ru.yandex.practicum.model.EndpointHit;
import ru.yandex.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final HitMapper hitMapper;

    public StatsServiceImpl(StatsRepository statsRepository, HitMapper hitMapper) {
        this.statsRepository = statsRepository;
        this.hitMapper = hitMapper;
    }

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto hitDto) {
        log.info("Сохранение информации о запросе: app={}, uri={}, ip={}",
                hitDto.getApp(), hitDto.getUri(), hitDto.getIp());

        validateHit(hitDto);

        EndpointHit savedHit = statsRepository.save(hitMapper.toEntity(hitDto));
        log.info("Информация о запросе сохранена с ID: {}", savedHit.getId());

        return hitMapper.toDto(savedHit);
    }

    private void validateHit(EndpointHitDto hitDto) {
        if (hitDto.getTimestamp() == null) {
            throw new ValidationException("Timestamp cannot be null");
        }
        if (hitDto.getTimestamp().isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new ValidationException("Timestamp cannot be too far in the future");
        }
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.warn("Запрошена статистика для неправильного временного диапазона: start={}, end={}", start, end);
            throw new ValidationException("Start date cannot be after end date");
        }
    }


    @Override
    public List<ViewStatsDto> getStats(StatsRequest request) {
        validateTimeRange(request.getStart(), request.getEnd());

        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}", request.getStart(), request.getEnd(),
                request.getUris().size(), request.isUnique());

        return statsRepository.getStats(request.getStart(), request.getEnd(), request.getUris(), request.isUnique());
    }
}
