package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;
import ru.yandex.practicum.dto.UserDto;

@FeignClient(name = "user-service", path = "/internal/users", configuration = FeignClientConfiguration.class)
public interface UserClient{
    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable Long userId);
}

