package ru.yandex.practicum.user.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import ru.yandex.practicum.grpc.user.action.UserActionProto;
import ru.yandex.practicum.user.model.UserAction;

@Mapper(componentModel = "spring")
public interface UserActionProtoMapper {

    @ValueMapping(source = "ACTION_VIEW", target = "VIEW")
    @ValueMapping(source = "ACTION_REGISTER", target = "REGISTER")
    @ValueMapping(source = "ACTION_LIKE", target = "LIKE")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.NULL)
    UserAction toModel(UserActionProto userActionProto);

    default java.time.Instant mapTime(com.google.protobuf.Timestamp timestamp) {
        return java.time.Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
