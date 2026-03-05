package practicum.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practicum.mapper.EventSimilarityAvroMapper;
import practicum.mapper.UserActionAvroMapper;
import practicum.model.EventSimilarity;
import practicum.model.RecommendedEvent;
import practicum.model.SimilarEventRequest;
import practicum.model.UserAction;
import practicum.repository.EventSimilarityRepository;
import practicum.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import practicum.model.UserPredictionsRequest;

import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AnalyzerService {
    private final UserActionAvroMapper userActionAvroMapper;
    private final EventSimilarityAvroMapper eventSimilarityAvroMapper;
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;


    public void getUserAction(UserActionAvro message) {
        UserAction userAction = userActionAvroMapper.toModel(message);
        userActionRepository.save(userAction);
    }

    public void getEventSimilarity(EventSimilarityAvro message) {
       EventSimilarity eventSimilarity = eventSimilarityAvroMapper.toModel(message);
       eventSimilarityRepository.save(eventSimilarity);
    }

    public List<RecommendedEvent> getSimilarEvents(SimilarEventRequest similarEventRequest) {
        Long searchId = similarEventRequest.getEventId();

        return eventSimilarityRepository.getSimilarEvents(similarEventRequest).stream()
                .map(e -> RecommendedEvent.builder()
                        .eventId(e.getEventA().equals(searchId) ? e.getEventB() : e.getEventA())
                        .score(e.getScore())
                        .build())
                .toList();
    }

    public List<RecommendedEvent> getRecommendationsForUser(UserPredictionsRequest userPredictionsRequest) {
        return eventSimilarityRepository.getRecommendationsForUser(userPredictionsRequest.getUserId(),
                userPredictionsRequest.getMaxResults());
    }

    public List<RecommendedEvent> getInteractionsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        return userActionRepository.getInteractionsCount(eventIds);
    }
}
