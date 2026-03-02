package ru.yandex.practicum.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.kafka.EventSimilarityPublisher;
import ru.yandex.practicum.mapper.UserActionAvroMapper;
import ru.yandex.practicum.model.AllWeights;
import ru.yandex.practicum.model.MinWeightsSums;
import ru.yandex.practicum.model.UserAction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SimilarityService {
    private final UserActionAvroMapper userActionAvroMapper;
    private final EventSimilarityPublisher eventSimilarityPublisher;
    private final AllWeights allWeights = new AllWeights();
    private final MinWeightsSums minWeightsSums = new MinWeightsSums();
    private final Map<Long, Double> weightsSums = new ConcurrentHashMap<>();


    public void recalculateSimilarity(UserActionAvro message) {
        UserAction userAction = userActionAvroMapper.toModel(message);
        long userId = userAction.getUserId();
        long eventA = userAction.getEventId();
        double newWeight = userAction.getActionType().getWeight();

        double oldWeight = allWeights.get(userId, eventA);

        if (newWeight <= oldWeight) {
            return;
        }

        double weightDelta = newWeight - oldWeight;

        weightsSums.compute(eventA, (k, v) ->
                v == null ? newWeight : v + weightDelta
        );

        Map<Long, Double> userEvents = allWeights.getMap(userId);

        Set<Long> affectedEvents = new HashSet<>();

        for (Map.Entry<Long, Double> entry : userEvents.entrySet()) {
            long eventB = entry.getKey();
            double weightB = entry.getValue();

            if (eventB == eventA) continue;

            double oldMin = Math.min(oldWeight, weightB);
            double newMin = Math.min(newWeight, weightB);
            double minDelta = newMin - oldMin;

            if (minDelta > 0) {
                minWeightsSums.addDelta(eventA, eventB, minDelta);
                affectedEvents.add(eventB);
            }
        }

        allWeights.put(userId, eventA, newWeight);

        double sa = weightsSums.get(eventA);

        for (Long eventB : affectedEvents) {
            double sMin = minWeightsSums.get(eventA, eventB);
            if (sMin == 0) continue;

            double sb = weightsSums.get(eventB);
            if (sa == 0 || sb == 0) continue;

            double similarity = getSimilarity(sa, sb, sMin);

            long firstEvent = Math.min(eventA, eventB);
            long secondEvent = Math.max(eventA, eventB);

            EventSimilarityAvro similarityAvro = EventSimilarityAvro.newBuilder()
                    .setEventA(firstEvent)
                    .setEventB(secondEvent)
                    .setScore(similarity)
                    .setTimestamp(message.getTimestamp())
                    .build();

            eventSimilarityPublisher.publish(similarityAvro);
        }
    }

    private static double getSimilarity(double Sa, double Sb, double Smin){
        return Smin / (Math.sqrt(Sa) * Math.sqrt(Sb));
    }
}
