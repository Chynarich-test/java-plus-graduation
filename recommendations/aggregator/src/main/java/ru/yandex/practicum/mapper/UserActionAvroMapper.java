package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.model.UserAction;

@Mapper(componentModel = "spring")
public interface UserActionAvroMapper {

    @Mapping(target = "timestamp", expression = "java(java.time.Instant.ofEpochMilli(userAction.getTimestamp()))")
    UserAction toModel(UserActionAvro userAction);
}
