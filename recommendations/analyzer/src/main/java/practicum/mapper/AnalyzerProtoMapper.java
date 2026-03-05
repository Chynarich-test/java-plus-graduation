package practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import practicum.model.InteractionsCountRequest;
import practicum.model.RecommendedEvent;
import practicum.model.SimilarEventRequest;
import practicum.model.UserPredictionsRequest;
import ru.yandex.practicum.grpc.event.recommendation.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.event.recommendation.RecommendedEventProto;
import ru.yandex.practicum.grpc.event.recommendation.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.event.recommendation.UserPredictionsRequestProto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnalyzerProtoMapper {

    SimilarEventRequest toModel(SimilarEventsRequestProto similarEventsRequestProto);

    RecommendedEvent toModel(RecommendedEventProto recommendedEventProto);

    RecommendedEventProto toProto(RecommendedEvent recommendedEventProto);

    UserPredictionsRequest toModel(UserPredictionsRequestProto userPredictionsRequest);

    List<InteractionsCountRequest> toModel(List<InteractionsCountRequest> interactionsCountRequestProto);
}
