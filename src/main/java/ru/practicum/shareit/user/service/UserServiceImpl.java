package ru.practicum.shareit.user.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.List;
import java.util.Set;

@Data
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    private static UserMapper userMapper;

    public User createUser(UserDto userDto) {
        validateUser(userDto);
        return userStorage.putUser(userMapper.mapToUser(userDto));
    }

    public User updateUser(UserDto userDto, int userId) {
        validateUser(userDto, userId);
        User oldUser = userStorage.getUserById(userId);

        if (userDto.getEmail() != null) {
            oldUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }
        validateUser(oldUser);
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
        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new ValidationException("User with email = " + userDto.getEmail() + " already exists");
        }
    }

    private void validateUser(UserDto userDto, int userId) {
        if (userStorage.existsByEmail(userDto.getEmail(), userId)) {
            throw new ValidationException("User with email = " + userDto.getEmail() + " already exists");
        }
    }

    private void validateUser(User oldUser) {
        Set<ConstraintViolation<User>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(oldUser);
        if (!violations.isEmpty()) {
            throw new ValidationException("User data not validated");
        }
    }
}
