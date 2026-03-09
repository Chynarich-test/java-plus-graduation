package ru.yandex.practicum.model;

import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class AllWeights {
    private Map<Long, Map<Long, Double>> allWeights = new ConcurrentHashMap<>();

    public void put(Long userId, Long eventId, Double newWeight) {
        allWeights
                .computeIfAbsent(userId, e -> new ConcurrentHashMap<>())
                .put(eventId, newWeight);
    }

    public double get(Long userId, Long eventId) {
        Map<Long, Double> userMap = allWeights.get(userId);
        if (userMap == null) return 0.0;
        return userMap.getOrDefault(eventId, 0.0);
    }

    public Map<Long, Double> getMap(Long userId) {
        return allWeights.getOrDefault(userId, Collections.emptyMap());
    }

}
