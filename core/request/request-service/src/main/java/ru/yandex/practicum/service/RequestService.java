package ru.yandex.practicum.service;



import ru.yandex.practicum.dto.ConfirmedRequestCount;
import ru.yandex.practicum.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.RequestDto;
import ru.yandex.practicum.dto.RequestStatus;

import java.util.List;

public interface RequestService {

    List<RequestDto> getUserRequests(Long userId);

    RequestDto createRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);

    List<ConfirmedRequestCount> countConfirmedRequests(List<Long> eventIds, RequestStatus status);
}
