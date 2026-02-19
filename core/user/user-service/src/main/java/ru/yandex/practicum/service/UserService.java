package ru.yandex.practicum.service;



import ru.yandex.practicum.dto.NewUserRequest;
import ru.yandex.practicum.dto.PageParams;
import ru.yandex.practicum.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    UserDto getUserById(Long id);

    List<UserDto> getUsers(List<Long> ids, PageParams pageParams);

    void deleteUser(Long id);

    UserDto updateUser(Long id, UserDto userDto);

}