package practicum.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import practicum.mapper.AnalyzerProtoMapper;
import practicum.model.InteractionsCountRequest;
import practicum.model.SimilarEventRequest;
import practicum.model.UserPredictionsRequest;
import practicum.service.AnalyzerService;
import ru.yandex.practicum.grpc.event.recommendation.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.event.recommendation.RecommendedEventProto;
import ru.yandex.practicum.grpc.event.recommendation.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.event.recommendation.UserPredictionsRequestProto;
import ru.yandex.practicum.grpc.recommendation.service.RecommendationsControllerGrpc;
import practicum.model.RecommendedEvent;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class AnalyzerController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final AnalyzerService analyzerService;
    private final AnalyzerProtoMapper analyzerProtoMapper;

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            SimilarEventRequest similarEventRequest = analyzerProtoMapper.toModel(request);

            List<RecommendedEvent> recommendedEvents = analyzerService.getSimilarEvents(similarEventRequest);

            for (RecommendedEvent event : recommendedEvents) {
                RecommendedEventProto proto = analyzerProtoMapper.toProto(event);
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
        } catch (Exception e){
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }


    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            UserPredictionsRequest userPredictionsRequest = analyzerProtoMapper.toModel(request);

            List<RecommendedEvent> recommendedEvents = analyzerService.getRecommendationsForUser(userPredictionsRequest);

            for (RecommendedEvent event : recommendedEvents) {
                RecommendedEventProto proto = analyzerProtoMapper.toProto(event);
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
        } catch (Exception e){
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<Long> interactionsCountRequest = request.getEventIdList();

            List<RecommendedEvent> recommendedEvents = analyzerService.getInteractionsCount(interactionsCountRequest);

            for (RecommendedEvent event : recommendedEvents) {
                RecommendedEventProto proto = analyzerProtoMapper.toProto(event);
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
        } catch (Exception e){
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
