package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;

@FeignClient(name = "user-service", path = "/admin/users", configuration = FeignClientConfiguration.class)
public interface UserAdminClient extends UserAdminOperations{
}
