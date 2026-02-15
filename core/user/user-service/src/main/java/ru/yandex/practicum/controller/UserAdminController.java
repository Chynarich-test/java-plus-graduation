package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.UserAdminOperations;
import ru.yandex.practicum.dto.NewUserRequest;
import ru.yandex.practicum.dto.PageParams;
import ru.yandex.practicum.dto.UserDto;
import ru.yandex.practicum.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserAdminController implements UserAdminOperations {

    private final UserService userService;

    @Override
    public UserDto registerUser(NewUserRequest request) {
        return userService.createUser(request);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageParams pageParams = new PageParams(from, size);
        return userService.getUsers(ids, pageParams);
    }

    @Override
    public UserDto getUser(Long userId) {
        return userService.getUserById(userId);
    }

    @Override
    public void deleteUser(Long userId) {
        userService.deleteUser(userId);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        return userService.updateUser(userId, userDto);
    }
}