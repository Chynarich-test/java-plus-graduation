package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.NewUserRequest;
import ru.yandex.practicum.dto.UserDto;

import java.util.List;

public interface UserAdminOperations {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto registerUser(@Valid @RequestBody NewUserRequest request);

    @GetMapping
    List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(name = "from", required = false, defaultValue = "0") int from,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size
    );

    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable Long userId);

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable Long userId);

    @PatchMapping("/{userId}")
    UserDto updateUser(@PathVariable Long userId, @RequestBody UserDto userDto);
}
