package practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import practicum.model.EventSimilarity;
import practicum.model.UserAction;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Mapper(componentModel = "spring")
public interface UserActionAvroMapper {
    @Mapping(target = "actionType", source = "actionType")
    UserAction toModel(UserActionAvro userAction);

    default Double mapActionType(ActionTypeAvro type) {
        if (type == null) return 0.0;
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

}
