package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test() {
        User user = User.builder()
                .name("Name1")
                .email("name1@mail.ru")
                .build();

        Long userId = userRepository.save(user).getId();
        User userTest = userRepository.findById(userId).get();

        assertEquals(user.getName(), userTest.getName());
        assertEquals(user.getEmail(), userTest.getEmail());
    }
}