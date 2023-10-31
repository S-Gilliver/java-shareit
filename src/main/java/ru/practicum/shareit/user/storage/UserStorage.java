package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {

    User putUser(User user);

    User updateUser(User user);

    User getUserById(int userId);

    List<User> getUsers();

    void removeUser(int userId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndId(String email, int userId);
}
