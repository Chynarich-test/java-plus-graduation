package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "stats-server")
public interface StatsClient extends StatsOperations {
}