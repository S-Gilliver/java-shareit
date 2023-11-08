package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    private User userTest1;

    private static final String USER_API = "/users";

    public ResultActions performUserGet(String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .get(api)
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    public ResultActions performUserPost(User userDtoTest, String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .post(api)
                .content(mapper.writeValueAsString(userDtoTest))
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    public ResultActions performUserPatch(User userDtoTest, String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .patch(api)
                .content(mapper.writeValueAsString(userDtoTest))
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }


    @BeforeEach
    public void setUp() {
        userTest1 = new User();
        userTest1.setId(1L);
        userTest1.setName("Name");
        userTest1.setEmail("test@mail.ru");
    }

    @Test
    void createUser() throws Exception {
        when(userService.createUser(Mockito.any(UserDto.class))).thenReturn(userTest1);

        ResultActions resultActions = performUserPost(userTest1, USER_API);

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id",
                        is(userTest1.getId()), Long.class))
                .andExpect(jsonPath("$.name",
                        is(userTest1.getName())))
                .andExpect(jsonPath("$.email",
                        is(userTest1.getEmail())));
    }

    @Test
    void saveUserShouldReturnBadRequest() throws Exception {
        when(userService.createUser(Mockito.any(UserDto.class)))
                .thenThrow(new BadRequestException("error"));

        ResultActions resultActions = performUserPost(userTest1, USER_API);
        resultActions
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser() throws Exception {
        userTest1.setName("rename");
        userTest1.setEmail("reemail@mail.ru");

        when(userService.updateUser(Mockito.any(UserDto.class),
                Mockito.anyLong())).thenReturn(userTest1);

        ResultActions resultActions = performUserPatch(userTest1, USER_API + "/1");

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userTest1.getId()),
                        Long.class))
                .andExpect(jsonPath("$.name", is(userTest1.getName())))
                .andExpect(jsonPath("$.email", is(userTest1.getEmail())));
    }

    @Test
    void updateUserShouldReturnNotFound() throws Exception {
        when(userService.updateUser(Mockito.any(UserDto.class),
                Mockito.anyLong())).thenThrow(new NotFoundException("error"));

        ResultActions resultActions = performUserPatch(userTest1, USER_API + "/1");

        resultActions
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById() throws Exception {
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest1);

        ResultActions resultActions = performUserGet(USER_API + "/1");

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userTest1.getId()),
                        Long.class))
                .andExpect(jsonPath("$.name", is(userTest1.getName())))
                .andExpect(jsonPath("$.email", is(userTest1.getEmail())));
    }

    @Test
    void getUserByIdShouldReturnNotFound() throws Exception {
        when(userService.getUserById(Mockito.anyLong()))
                .thenThrow(new NotFoundException("error"));

        ResultActions resultActions = performUserGet(USER_API + "/1");

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void removeUser() throws Exception {
        mvc.perform(delete(USER_API + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}