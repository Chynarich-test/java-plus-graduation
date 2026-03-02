package ru.yandex.practicum.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.user.model.UserAction;

@Mapper(componentModel = "spring")
public interface UserActionAvroMapper {

    UserActionAvro toAvro(UserAction userAction);
}
