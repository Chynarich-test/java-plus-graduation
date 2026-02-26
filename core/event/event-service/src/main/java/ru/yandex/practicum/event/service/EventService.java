package ru.yandex.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.event.client.RequestClient;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.event.client.UserClient;
import ru.yandex.practicum.common.dsl.validator.EntityValidator;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.event.dao.EventRepository;
import ru.yandex.practicum.event.dto.*;
import ru.yandex.practicum.event.dto.enums.AdminStateAction;
import ru.yandex.practicum.event.dto.enums.EventSort;
import ru.yandex.practicum.event.dto.enums.UserStateAction;
import ru.yandex.practicum.event.dto.request.AdminEventFilter;
import ru.yandex.practicum.event.dto.request.PublicEventFilter;
import ru.yandex.practicum.event.dto.request.UserEventsQuery;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.dto.enums.EventState;
import ru.yandex.practicum.common.dsl.exception.ExistException;
import ru.yandex.practicum.common.dsl.exception.InvalidDateRangeException;
import ru.yandex.practicum.common.dsl.exception.NotFoundException;
import ru.yandex.practicum.common.dsl.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final StatsClient statsClient;
    private final HttpServletRequest request;
    private final EntityValidator entityValidator;
    private final RequestClient requestClient;
    private final TransactionTemplate transactionTemplate;

    public List<EventShortDto> findEvents(UserEventsQuery query) {
        List<EventShortDto> dtos = eventMapper.toEventsShortDto(eventRepository.findByInitiatorId(query.userId(),
                PageRequest.of(query.from() / query.size(), query.size())));

        if (dtos != null && !dtos.isEmpty()) {
            List<String> uris = dtos.stream()
                    .map(d -> "/events/" + d.getId())
                    .collect(Collectors.toList());
            saveHit();
            Map<String, Long> hits = fetchHitsForUris(uris);
            for (EventShortDto dto : dtos) {
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
            }
        }

        return dtos;
    }

    private UserShortDto getShortUser(long userId){
        UserDto owner = userClient.getUser(userId);
        return UserShortDto.builder()
                .name(owner.getName())
                .id(owner.getId()).build();
    }

    public EventFullDto createEvent(long userId, NewEventDto eventDto) {
        UserShortDto owner = getShortUser(userId);

        return transactionTemplate.execute(status -> {
            if (eventDto.getEventDate() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (eventDto.getEventDate().isBefore(now.plusHours(2))) {
                    throw new ValidationException("Event date must be at least 2 hours in the future");
                }
            }

            Event event = eventMapper.fromNewEventDto(eventDto);
            event.setInitiatorId(owner.getId());
            event.setState(EventState.PENDING);

            Event savedItem = eventRepository.save(event);
            return eventMapper.toEventFullDto(savedItem, owner);
        });
    }


    public EventFullDto findUserEventById(long userId, long eventId) {
        UserShortDto owner = getShortUser(userId);
        EventFullDto dto = eventMapper.toEventFullDto(findByIdAndUser(eventId, userId), owner);
        if (dto != null) {
            String uri = "/events/" + dto.getId();
            Map<String, Long> hits = fetchHitsForUris(List.of(uri));
            dto.setViews(hits.getOrDefault(uri, 0L));
        }
        return dto;
    }

    private Event findByPublicId(long eventId) {
        return eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event findByIdAndUser(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Владелец с ID " + userId + " или ивент с ID " + eventId + " не найдены"));
    }

    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        UserShortDto owner = getShortUser(userId);
        return transactionTemplate.execute(status -> {
            Event event = findByIdAndUser(eventId, userId);
            if (event.getState().equals(EventState.PUBLISHED)) {
                throw new ExistException("Only pending or canceled events can be changed");
            }

            if (updateRequest.getEventDate() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (updateRequest.getEventDate().isBefore(now.plusHours(2))) {
                    throw new ValidationException("Event date must be at least 2 hours in the future");
                }
            }

            eventMapper.updateEventFromUserDto(updateRequest, event);

            if (updateRequest.getStateAction() != null) {
                if (event.getState().equals(EventState.CANCELED) &&
                        updateRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                    event.setState(EventState.PENDING);
                } else if (event.getState().equals(EventState.PENDING) &&
                        updateRequest.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                    event.setState(EventState.CANCELED);
                }
            }

            return eventMapper.toEventFullDto(eventRepository.save(event), owner);
        });
    }

    public List<EventShortDto> searchPublicEvents(PublicEventFilter filter) {
        if (filter.getRangeStart() != null && filter.getRangeEnd() != null) {
            if (filter.getRangeStart().isAfter(filter.getRangeEnd())) {
                throw new InvalidDateRangeException("Дата начала не может быть позже даты окончания.");
            }
        }

        List<EventShortDto> dtos = eventMapper.toEventsShortDto(eventRepository.searchEventsByPublic(filter));

        if (dtos != null && !dtos.isEmpty()) {
            List<String> uris = dtos.stream().map(d -> "/events/" + d.getId()).collect(Collectors.toList());
            Map<String, Long> hits = fetchHitsForUris(uris);
            for (EventShortDto dto : dtos) {
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
            }
        }

        saveHit();

        if (filter.getSort() != null && filter.getSort() == EventSort.VIEWS) {
            dtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return dtos;
    }

    public EventFullDto findPublicEventById(long id) {
        Event event = findByPublicId(id);

        UserShortDto owner = getShortUser(event.getInitiatorId());


        EventFullDto dto = eventMapper.toEventFullDto(event, owner);
        if (dto != null) {
            saveHit();
            String uri = "/events/" + dto.getId();
            Map<String, Long> hits = fetchHitsForUris(List.of(uri));
            dto.setViews(hits.getOrDefault(uri, 0L));
        }

        return dto;
    }

    public List<EventFullDto> searchEventsByAdmin(AdminEventFilter filter) {
        List<EventFullDto> dtos = eventMapper.toEventsFullDto(eventRepository.searchEventsByAdmin(filter));

        setConfirmedRequestsForEvents(dtos);

        if (dtos != null && !dtos.isEmpty()) {
            List<String> uris = dtos.stream()
                    .map(d -> "/events/" + d.getId())
                    .collect(Collectors.toList());
            Map<String, Long> hits = fetchHitsForUris(uris);
            for (EventFullDto dto : dtos) {
                dto.setViews(hits.getOrDefault("/events/" + dto.getId(), 0L));
            }
        }


        return dtos;
    }

    private void setConfirmedRequestsForEvents(List<EventFullDto> dtos) {
        List<Long> eventIds = dtos.stream()
                .map(EventFullDto::getId)
                .toList();

        List<ConfirmedRequestCount> requestCounts =
                requestClient.getConfirmedCounts(eventIds, RequestStatus.CONFIRMED);

        Map<Long, Long> confirmedRequestsMap = requestCounts.stream()
                .collect(Collectors.toMap(ConfirmedRequestCount::getEventId, ConfirmedRequestCount::getCount));

        dtos.forEach(dto -> dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0L)));
    }

    public EventFullDto moderateEvent(Long eventId, UpdateEventAdminRequest adminRequest) {

        Event eventInitial = entityValidator.ensureAndGet(eventRepository, eventId, "Event");

        UserShortDto owner = getShortUser(eventInitial.getInitiatorId());

        return transactionTemplate.execute(status -> {
            Event event = entityValidator.ensureAndGet(eventRepository, eventId, "Event");

            if (adminRequest.getEventDate() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (adminRequest.getEventDate().isBefore(now.plusHours(1))) {
                    throw new ExistException("Event date must be at least one hour in the future to publish.");
                }
            }

            eventMapper.updateEventFromAdminDto(adminRequest, event);

            if (adminRequest.getStateAction() != null) {
                if (event.getState().equals(EventState.PENDING)) {
                    if (adminRequest.getStateAction().equals(AdminStateAction.PUBLISH_EVENT))
                        event.setState(EventState.PUBLISHED);
                    if (adminRequest.getStateAction().equals(AdminStateAction.REJECT_EVENT))
                        event.setState(EventState.CANCELED);
                } else {
                    throw new ExistException("Cannot publish the event because it's not in the right state: PUBLISHED");
                }
            }

            return eventMapper.toEventFullDto(eventRepository.save(event), owner);
        });
    }

    private void saveHit() {
        try {
            EndpointHitDto hitDto = EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.saveHit(hitDto);
        } catch (Exception e) {
            log.error("Не удалось отправить информацию о просмотре в сервис статистики: {}", e.getMessage());
        }
    }

    private Map<String, Long> fetchHitsForUris(List<String> uris) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(10);
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            List<ViewStatsDto> stats = statsClient.getStats(StatsRequest.builder()
                    .start(start)
                    .end(end)
                    .uris(uris)
                    .unique(true)
                    .build());
            if (stats == null || stats.isEmpty()) return Map.of();
            return stats.stream().collect(Collectors.toMap(
                    ViewStatsDto::getUri, v -> v.getHits() == null ? 0L : v.getHits()));
        } catch (Exception e) {
            log.error("Не удалось получить просмотры из сервиса статистики: {}", e.getMessage());
            return Map.of();
        }
    }

    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        return requestClient.getEventRequests(userId, eventId);
    }

    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        return requestClient.updateStatuses(userId, eventId, request);
    }

    public List<EventFullDto> findAllById(List<Long> ids){
        return eventMapper.toEventsFullDto(eventRepository.findAllById(ids));
    }
}
