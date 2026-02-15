package ru.yandex.practicum.event.client;


import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;

@FeignClient(name = "event-service", path = "/users/{userId}/events",
        contextId = "eventPrivateClient", configuration = FeignClientConfiguration.class)
public interface EventPrivateClient extends EventPrivateOperations{
}
