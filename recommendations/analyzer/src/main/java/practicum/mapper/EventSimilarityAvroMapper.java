package practicum.mapper;

import org.mapstruct.Mapper;
import practicum.model.EventSimilarity;
import practicum.model.UserAction;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Mapper(componentModel = "spring")
public interface EventSimilarityAvroMapper {

    EventSimilarity toModel(EventSimilarityAvro eventSimilarityAvro);

}
