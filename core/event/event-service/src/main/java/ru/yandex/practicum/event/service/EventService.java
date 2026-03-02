package ru.yandex.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.client.AnalyzerClient;
import ru.yandex.practicum.client.CollectorClient;
import ru.yandex.practicum.event.client.RequestClient;
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
import ru.yandex.practicum.grpc.event.recommendation.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final HttpServletRequest request;
    private final EntityValidator entityValidator;
    private final RequestClient requestClient;
    private final TransactionTemplate transactionTemplate;
    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;

    @Value("${app.recommendations.max-results}")
    private int maxResults;

    public List<EventShortDto> findEvents(UserEventsQuery query) {
        List<EventShortDto> dtos = eventMapper.toEventsShortDto(eventRepository.findByInitiatorId(query.userId(),
                PageRequest.of(query.from() / query.size(), query.size())));

        enrichShortWithRatings(dtos);

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
        enrichFullWithRatings(List.of(dto));
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

        enrichShortWithRatings(dtos);

        if (filter.getSort() != null && filter.getSort() == EventSort.RATING) {
            dtos.sort(Comparator.comparing(EventShortDto::getRating).reversed());
        }

        return dtos;
    }

    public EventFullDto findPublicEventById(long id, Long userId) {
        Event event = findByPublicId(id);

        UserShortDto owner = getShortUser(event.getInitiatorId());

        EventFullDto dto = eventMapper.toEventFullDto(event, owner);

        if (userId != null && userId > 0) {
            collectorClient.collectUserAction(userId, id, UserActionType.VIEW);
        }

        enrichFullWithRatings(List.of(dto));

        return dto;
    }

    public List<EventFullDto> searchEventsByAdmin(AdminEventFilter filter) {
        List<EventFullDto> dtos = eventMapper.toEventsFullDto(eventRepository.searchEventsByAdmin(filter));

        setConfirmedRequestsForEvents(dtos);

        enrichFullWithRatings(dtos);

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



    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        return requestClient.getEventRequests(userId, eventId);
    }

    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        return requestClient.updateStatuses(userId, eventId, request);
    }

    public List<EventFullDto> findAllById(List<Long> ids){
        return eventMapper.toEventsFullDto(eventRepository.findAllById(ids));
    }

    public List<EventFullDto> getRecommendations(long userId) {
        List<RecommendedEventProto> recommendations = analyzerClient.getRecommendationsForUser(userId, maxResults).toList();

        if (recommendations.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = recommendations.stream()
                .map(RecommendedEventProto::getEventId)
                .toList();

        List<Event> events = eventRepository.findAllById(eventIds);

        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        Map<Long, UserShortDto> usersMap = userClient.getUsers(initiatorIds, 0, initiatorIds.size()).stream()
                .collect(Collectors.toMap(
                        UserDto::getId,
                        u -> UserShortDto.builder().id(u.getId()).name(u.getName()).build()
                ));

        Map<Long, Double> scores = recommendations.stream()
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));

        Map<Long, EventFullDto> dtoMap = events.stream()
                .map(event -> {
                    UserShortDto owner = usersMap.get(event.getInitiatorId());
                    EventFullDto dto = eventMapper.toEventFullDto(event, owner);
                    dto.setRating(scores.getOrDefault(event.getId(), 0.0));
                    return dto;
                })
                .collect(Collectors.toMap(EventFullDto::getId, d -> d));

        return eventIds.stream()
                .filter(dtoMap::containsKey)
                .map(dtoMap::get)
                .toList();
    }

    public void likeEvent(long eventId, long userId) {
        Event event = entityValidator.ensureAndGet(eventRepository, eventId, "Event");

        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Не может быть лайкнуто событие, которое еще не произошло");
        }

        List<RequestDto> requests = requestClient.getEventRequests(userId, eventId);

        boolean hasAttended = requests != null && requests.stream()
                .anyMatch(r -> RequestStatus.CONFIRMED.name().equals(r.getStatus()));

        if (!hasAttended) {
            throw new ValidationException("Пользователю могут нравиться только посещенные мероприятия");
        }

        collectorClient.collectUserAction(userId, eventId, UserActionType.LIKE);
    }

    private void enrichShortWithRatings(List<EventShortDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        List<Long> eventIds = dtos.stream().map(EventShortDto::getId).toList();

        Map<Long, Double> ratings;

        try {
            ratings = analyzerClient.getInteractionsCount(eventIds)
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            RecommendedEventProto::getScore
                    ));
        } catch (Exception e) {
            ratings = Map.of();
        }

        for (EventShortDto dto : dtos) {
            dto.setRating(ratings.getOrDefault(dto.getId(), 0.0));
        }
    }

    private void enrichFullWithRatings(List<EventFullDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        List<Long> eventIds = dtos.stream().map(EventFullDto::getId).toList();

        Map<Long, Double> ratings;

        try {
            ratings = analyzerClient.getInteractionsCount(eventIds)
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            RecommendedEventProto::getScore
                    ));
        } catch (Exception e) {
            ratings = Map.of();
        }

        for (EventFullDto dto : dtos) {
            dto.setRating(ratings.getOrDefault(dto.getId(), 0.0));
        }
    }
}
