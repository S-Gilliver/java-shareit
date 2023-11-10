package ru.practicum.shareit.user.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.List;
import java.util.Set;

@Slf4j
@Builder
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static UserMapper userMapper;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(UserDto userDto) {
        User user = userMapper.mapToUser(userDto);
        validateUserConstraints(user);
        log.info("user successfully added");
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(UserDto userDto, Long userId) {
        User oldUser = getUserById(userId);

        if (userDto.getEmail() != null) {
            oldUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            oldUser.setName(userDto.getName());
        }
        log.info("user successfully updated");
        return userRepository.save(oldUser);
    }

    @Override
    @Transactional
    public User getUserById(Long userId) {
        validateUser(userId);
        return userRepository.findById(userId).get();
    }

    @Override
    @Transactional
    public List<User> getUsers() {
        log.info("user list has been successfully received");
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void removeUser(Long id) {
        log.info("user successfully deleted");
        userRepository.deleteById(id);
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id = " + userId + " doesn't exist");
        }
    }

    private static void validateUserConstraints(User oldUser) {
        Set<ConstraintViolation<User>> violations = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .validate(oldUser);

        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder("User data not validated: ");
            for (ConstraintViolation<?> violation : violations) {
                message.append(violation.getMessage()).append(";");
            }
            throw new BadRequestException(message.toString());
        }
    }
}
