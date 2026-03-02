package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.model.UserAction;

@Mapper(componentModel = "spring")
public interface UserActionAvroMapper {

    UserAction toModel(UserActionAvro userAction);
}
