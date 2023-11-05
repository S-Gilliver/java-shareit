package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

     public static User mapToUser(UserDto userDto) {
         User user = new User();
         user.setEmail(userDto.getEmail());
         user.setName(userDto.getName());
         return user;
    }
}
