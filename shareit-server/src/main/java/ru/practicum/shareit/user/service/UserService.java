package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(UserDto userDto);

    User updateUser(UserDto userDto, Long userId);

    User getUserById(Long userId);

    List<User> getUsers();

    void removeUser(Long id);

}
