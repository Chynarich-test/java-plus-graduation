package ru.yandex.practicum.event.client;


import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;

@FeignClient(name = "event-service", path = "/admin/events",
        contextId = "eventAdminClient", configuration = FeignClientConfiguration.class)
public interface EventAdminClient extends EventAdminOperations{
}
