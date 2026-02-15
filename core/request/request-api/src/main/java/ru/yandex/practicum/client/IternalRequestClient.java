package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;

@FeignClient(name = "request-service", path = "/internal/requests",
        contextId = "iternalRequestClient", configuration = FeignClientConfiguration.class)
public interface IternalRequestClient extends IternalRequestOperations{
}
