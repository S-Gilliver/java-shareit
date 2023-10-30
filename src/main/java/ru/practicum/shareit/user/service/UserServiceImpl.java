package ru.practicum.shareit.user.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.List;
import java.util.Set;

import static ru.practicum.shareit.user.mapper.UserMapper.mapToUser;

@Data
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    public User createUser(UserDto userDto) {
        validateUser(userDto);
        return userStorage.putUser(mapToUser(userDto));
    }

    public User updateUser(UserDto userDto, int userId) {
        User oldUser = validateUserById(userDto, userId);
        return userStorage.updateUser(oldUser);
    }

    public User getUserById(int userId) {
        return userStorage.getUserById(userId);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void removeUser(int id) {
        userStorage.removeUser(id);
    }

    private void validateUser(UserDto userDto) {
        List<User> users = userStorage.getUsers();

        for (User user : users) {
            if (user.getEmail().equals(userDto.getEmail())) {
                throw new ValidationException("User with email = " + userDto.getEmail() + " already exists");
            }
        }
    }

    private User validateUserById(UserDto userDto, int userId) {
        List<User> users = userStorage.getUsers();
        User oldUser = userStorage.getUserById(userId);


        for (User user : users) {
            if (userId != user.getId()) {
                if (user.getEmail().equals(userDto.getEmail())) {
                    throw new ValidationException("User with email = " + userDto.getEmail() + " already exists");
                }
            }
        }

        if (userDto.getEmail() != null) {
            oldUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }

        Set<ConstraintViolation<User>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(oldUser);
        if (!violations.isEmpty()) {
            throw new ValidationException("User data not validated");
        }
        return oldUser;
    }
}
