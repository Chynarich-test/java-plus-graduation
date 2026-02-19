package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.EventClient;
import ru.yandex.practicum.common.dsl.validator.EntityValidator;
import ru.yandex.practicum.common.dsl.exception.ConflictException;
import ru.yandex.practicum.common.dsl.exception.NotFoundException;
import ru.yandex.practicum.dto.CompilationDto;
import ru.yandex.practicum.dto.NewCompilationDto;
import ru.yandex.practicum.dto.UpdateCompilationRequest;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.mapper.CompilationMapper;
import ru.yandex.practicum.model.Compilation;
import ru.yandex.practicum.repository.CompilationRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EntityValidator entityValidator;
    private final EventClient eventClient;
    private final TransactionTemplate transactionTemplate;

    @Override
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        log.info("Создание новой подборки с названием: {}", compilationDto.getTitle());

        List<EventFullDto> events = getEventsByIds(compilationDto.getEvents());

        // Проверка уникальности названия
        Compilation savedCompilation = transactionTemplate.execute(status -> {
            if (compilationRepository.existsByTitle(compilationDto.getTitle())) {
                throw new ConflictException("Подборка с названием='" + compilationDto.getTitle() + "' уже существует");
            }
            Compilation compilation = compilationMapper.toEntity(compilationDto);
            return compilationRepository.save(compilation);
        });

        log.info("Подборка создана с id: {}", savedCompilation.getId());
        return compilationMapper.toDto(savedCompilation, events);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        log.info("Обновление подборки с id: {}", compId);

        List<EventFullDto> events = getEventsByIds(request.getEvents());

        Compilation updatedCompilation = transactionTemplate.execute(status -> {
            Compilation compilation = entityValidator.ensureAndGet(
                    compilationRepository, compId, "Подборка"
            );

            if (request.getTitle() != null && !request.getTitle().equals(compilation.getTitle())) {
                if (compilationRepository.existsByTitle(request.getTitle())) {
                    throw new ConflictException("Подборка с названием='" + request.getTitle() + "' уже существует");
                }
                compilation.setTitle(request.getTitle());
            }

            if (request.getPinned() != null) {
                compilation.setPinned(request.getPinned());
            }

            if (request.getEvents() != null) {
                compilation.setEventIds(events.stream().map(EventFullDto::getId).collect(Collectors.toSet()));
            }

            return compilationRepository.save(compilation);
        });

        log.info("Подборка обновлена с id: {}", compId);

        return compilationMapper.toDto(updatedCompilation, events);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id: {}", compId);

        // Проверяем существование перед удалением
        entityValidator.ensureExists(compilationRepository, compId, "Подборка");

        compilationRepository.deleteById(compId);
        log.info("Подборка удалена с id: {}", compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение подборки с pinned={}, from={}, size={}", pinned, from, size);

        List<Compilation> compilations = transactionTemplate.execute(status -> {
            Pageable pageable = PageRequest.of(from / size, size);
            Page<Compilation> compilationsPage = compilationRepository.findByPinned(pinned, pageable);

            return compilationsPage.getContent();
        });

        Set<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEventIds().stream())
                .collect(Collectors.toSet());


        Map<Long, EventFullDto> eventsMap = new HashMap<>();
        if (!allEventIds.isEmpty()) {
            eventsMap = eventClient.getEventsByIds(new ArrayList<>(allEventIds))
                    .stream()
                    .collect(Collectors.toMap(EventFullDto::getId, e -> e));
        }

        final Map<Long, EventFullDto> finalEventsMap = eventsMap;
        return compilations.stream()
                .map(comp -> {
                    List<EventFullDto> compEvents = comp.getEventIds().stream()
                            .map(finalEventsMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return compilationMapper.toDto(comp, compEvents);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Получение подборки по id: {}", compId);

        Compilation compilation = transactionTemplate.execute(status ->
                entityValidator.ensureAndGet(compilationRepository, compId, "Подборка")
        );

        List<EventFullDto> events = eventClient.getEventsByIds(compilation.getEventIds().stream().toList());

        return compilationMapper.toDto(compilation, events);
    }

    private List<EventFullDto> getEventsByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        List<EventFullDto> events = eventClient.getEventsByIds(eventIds);

        // Проверяем, что все события найдены
        if (events.size() != eventIds.size()) {
            List<Long> foundIds = events.stream().map(EventFullDto::getId).toList();
            List<Long> missingIds = eventIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new NotFoundException("События с id=" + missingIds + " не найдены");
        }

        return events;
    }
}