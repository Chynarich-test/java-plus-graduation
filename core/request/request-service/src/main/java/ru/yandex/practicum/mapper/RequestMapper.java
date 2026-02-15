package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.RequestDto;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.dto.RequestStatus;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "status", expression = "java(request.getStatus().name())")
    RequestDto toDto(Request request);

    List<RequestDto> toDtoList(List<Request> requests);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", source = "event")
    @Mapping(target = "requesterId", source = "requester")
    @Mapping(target = "status", source = "status")
    Request toNewEntity(Long event, Long requester, RequestStatus status);

}