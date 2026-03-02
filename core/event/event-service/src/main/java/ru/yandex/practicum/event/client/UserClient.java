package ru.yandex.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.common.dsl.exception.decode.FeignClientConfiguration;
import ru.yandex.practicum.dto.UserDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/internal/users", configuration = FeignClientConfiguration.class)
public interface UserClient{
    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable Long userId);

    @GetMapping
    List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                  @RequestParam(name = "size", required = false, defaultValue = "10") int size);
}
