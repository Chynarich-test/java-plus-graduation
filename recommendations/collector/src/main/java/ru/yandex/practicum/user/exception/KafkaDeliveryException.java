package ru.yandex.practicum.user.exception;

public class KafkaDeliveryException extends RuntimeException {
    public KafkaDeliveryException(String message) {
        super(message);
    }
}
