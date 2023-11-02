package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User createUser(UserDto userDto);

    User updateUser(UserDto userDto, int userId);

    User getUserById(int userId);

    List<User> getUsers();

    void removeUser(int id);

}
