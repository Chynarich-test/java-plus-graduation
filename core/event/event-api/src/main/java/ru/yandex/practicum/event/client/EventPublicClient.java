package ru.yandex.practicum.event.client;


import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;

@FeignClient(name = "event-service", path = "/events", contextId = "eventPublicClient",
        configuration = FeignClientConfiguration.class)
public interface EventPublicClient extends EventPublicOperations{
}
