package ru.yandex.practicum.model;

public enum ActionType {

    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1);

    private final double weight;

    ActionType(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}

