package ru.yandex.practicum.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class MinWeightsSums {
    private Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    public void put(long eventA, long eventB, double sum) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .put(second, sum);
    }

    public double get(long eventA, long eventB) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .getOrDefault(second, 0.0);
    }

    public void addDelta(long eventA, long eventB, double delta) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .compute(second, (k, v) -> v == null ? delta : v + delta);
    }
}
