package ru.yandex.practicum.event.client;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service", path = "/internal/events", contextId = "eventInternalClient")
public interface EventInternalClient extends EventInternalOperations{
}
