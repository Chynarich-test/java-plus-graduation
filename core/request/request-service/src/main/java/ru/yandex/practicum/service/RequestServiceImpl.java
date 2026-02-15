package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.UserAdminClient;
import ru.yandex.practicum.common.dsl.validator.EntityValidator;
import ru.yandex.practicum.common.dsl.exception.ConflictException;
import ru.yandex.practicum.common.dsl.exception.ExistException;
import ru.yandex.practicum.common.dsl.exception.NotFoundException;
import ru.yandex.practicum.common.dsl.exception.ValidationException;
import ru.yandex.practicum.dto.ConfirmedRequestCount;
import ru.yandex.practicum.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.RequestDto;
import ru.yandex.practicum.event.client.EventInternalClient;
import ru.yandex.practicum.event.client.EventPublicClient;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.event.dto.enums.EventState;
import ru.yandex.practicum.mapper.RequestMapper;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.dto.RequestStatus;
import ru.yandex.practicum.repository.RequestRepository;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserAdminClient userAdminClient;
    private final EventPublicClient eventPublicClient;
    private final RequestMapper mapper;
    private final EntityValidator entityValidator;
    private final EventInternalClient eventInternalClient;

    private void CheckExistsUser(Long userId){
        userAdminClient.getUser(userId);
    }

    @Override
    public List<RequestDto> getUserRequests(Long userId) {
        CheckExistsUser(userId);
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return mapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public RequestDto createRequest(Long userId, Long eventId) {
        CheckExistsUser(userId);
        EventFullDto event = eventInternalClient.getEventsByIds(List.of(eventId)).getFirst();

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Инициатор события не может создать заявку на участие в своём же событии");
        }

        if (event.getState() == null || event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Нельзя добавить заявку: событие не опубликовано");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ExistException("Заявка от этого пользователя на это событие уже существует");
        }

        long confirmed = requestRepository.countConfirmedRequests(eventId);
        Integer limit = event.getParticipantLimit();
        if (limit != null && limit > 0 && confirmed >= limit) {
            throw new ConflictException("Достигнут лимит участников события");
        }

        RequestStatus initialStatus = RequestStatus.PENDING;
        if (!event.isRequestModeration() || limit == null || limit == 0) {
            initialStatus = RequestStatus.CONFIRMED;
        }

        Request request = mapper.toNewEntity(event.getId(), userId, initialStatus);

        LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        request.setCreated(creationTime);

        Request saved = requestRepository.save(request);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = entityValidator.ensureAndGet(requestRepository, requestId, "Заявка");

        if (!Objects.equals(request.getRequesterId(), userId)) {
            throw new ValidationException("Пользователь может отменять только свои заявки");
        }

        request.setStatus(RequestStatus.CANCELED);
        Request saved = requestRepository.save(request);
        return mapper.toDto(saved);
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        CheckExistsUser(userId);
        EventFullDto event = eventPublicClient.findPublicEventById(eventId);

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException("Только инициатор может просматривать заявки данного события");
        }

        List<Request> requests = requestRepository.findAllByEventIdWithRelations(eventId);
        return mapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        CheckExistsUser(userId);
        EventFullDto event = eventPublicClient.findPublicEventById(eventId);

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ValidationException("Только инициатор может менять статусы заявок");
        }

        String statusStr = Optional.ofNullable(updateRequest.getStatus())
                .orElse("")
                .toUpperCase(Locale.ROOT);
        RequestStatus targetStatus;
        try {
            targetStatus = RequestStatus.valueOf(statusStr);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Недопустимый статус: " + updateRequest.getStatus());
        }

        if (!(targetStatus == RequestStatus.CONFIRMED || targetStatus == RequestStatus.REJECTED)) {
            throw new ValidationException("Можно массово устанавливать только CONFIRMED или REJECTED");
        }

        List<Long> ids = Optional.ofNullable(updateRequest.getRequestIds())
                .orElse(Collections.emptyList());

        if (ids.isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(Collections.emptyList())
                    .rejectedRequests(Collections.emptyList())
                    .build();
        }

        List<Request> requests = requestRepository.findByEventIdAndIdInWithRelations(eventId, ids);

        long confirmedNow = requestRepository.countConfirmedRequests(eventId);
        Integer limit = event.getParticipantLimit();

        if (targetStatus == RequestStatus.CONFIRMED && limit != null && limit > 0) {
            long pendingToConfirm = requests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .count();
            long available = limit - confirmedNow;
            if (available <= 0 || pendingToConfirm > available) {
                throw new ConflictException("Достигнут лимит участников события");
            }
        }

        if (targetStatus == RequestStatus.REJECTED) {
            boolean anyNonPending = requests.stream()
                    .anyMatch(r -> r.getStatus() != RequestStatus.PENDING);
            if (anyNonPending) {
                throw new ConflictException("Нельзя отклонить заявку: она уже обработана");
            }
        }

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        if (targetStatus == RequestStatus.CONFIRMED) {
            for (Request req : requests) {
                if (req.getStatus() != RequestStatus.PENDING) continue;

                if (limit == null || limit == 0 || confirmedNow < limit) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmedNow++;
                    confirmed.add(req);
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(req);
                }
            }
        } else {
            for (Request req : requests) {
                if (req.getStatus() == RequestStatus.PENDING) {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(req);
                }
            }
        }

        confirmed.forEach(requestRepository::save);
        rejected.forEach(requestRepository::save);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(mapper.toDtoList(confirmed))
                .rejectedRequests(mapper.toDtoList(rejected))
                .build();
    }

    @Override
    public List<ConfirmedRequestCount> countConfirmedRequests(List<Long> eventIds, RequestStatus status) {
        return requestRepository.countConfirmedRequestsForEvents(eventIds, status);
    }
}