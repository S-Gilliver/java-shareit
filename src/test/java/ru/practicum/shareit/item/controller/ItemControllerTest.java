package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDtoTest;

    private ItemDtoBooking itemDtoBookingTest;

    private Comment comment;

    private static final String ITEM_API = "/items";

    public ResultActions performItemGet(String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .get(api)
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    public ResultActions performItemPost(ItemDto itemDtoTest, String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .post(api)
                .content(mapper.writeValueAsString(itemDtoTest))
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    public ResultActions performItemPatch(ItemDto itemDtoTest, String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .patch(api)
                .content(mapper.writeValueAsString(itemDtoTest))
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    @BeforeEach
    public void setUp() {
        itemDtoTest = ItemDto.builder()
                .id(1L)
                .name("item")
                .description("itemDescription")
                .available(true)
                .requestId(2L)
                .build();

        itemDtoBookingTest = ItemDtoBooking.builder()
                .id(2L)
                .name("itemBooking")
                .description("itemDescriptionBooking")
                .available(true)
                .build();

        User user = User.builder()
                .name("user")
                .email("email@mail.ru")
                .build();

        comment = new Comment();
        comment.setId(1L);
        comment.setText("comment");
        comment.setAuthor(user);
    }


    @Test
    public void createItem() throws Exception {
        when(itemService.createItem(Mockito.any(ItemDto.class),
                Mockito.anyLong())).thenReturn(itemDtoTest);

        ResultActions resultActions = performItemPost(itemDtoTest, ITEM_API);

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoTest.getId()),
                        Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoTest.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoTest.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoTest.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDtoTest.getRequestId()),
                        Long.class));
    }

    @Test
    public void createItemShouldReturnBadRequest() throws Exception {
        itemDtoTest.setName("");

        ResultActions resultActions = performItemPost(itemDtoTest, ITEM_API);

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void updateItem() throws Exception {
        itemDtoTest.setName("rename");
        itemDtoTest.setDescription("redescription");

        when(itemService.updateItem(Mockito.any(ItemDto.class),
                Mockito.anyLong())).thenReturn(itemDtoTest);

        ResultActions resultActions = performItemPatch(itemDtoTest, ITEM_API + "/1");

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoTest.getId()),
                        Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoTest.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoTest.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoTest.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDtoTest.getRequestId()),
                        Long.class));
    }

    @Test
    public void updateItemShouldReturnNotFound() throws Exception {
        when(itemService.updateItem(Mockito.any(ItemDto.class),
                Mockito.anyLong())).thenThrow(new NotFoundException("error"));

        ResultActions resultActions = performItemPatch(itemDtoTest, ITEM_API + "/1");
        resultActions.andExpect(status().isNotFound());
    }


    @Test
    public void getItemWithBookingById() throws Exception {
        when(itemService.getItemById(Mockito.anyLong(),
                Mockito.anyLong())).thenReturn(itemDtoBookingTest);

        ResultActions resultActions = performItemGet(ITEM_API + "/1");
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoBookingTest.getId()),
                        Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoBookingTest.getName())))
                .andExpect(jsonPath("$.description",
                        is(itemDtoBookingTest.getDescription())))
                .andExpect(jsonPath("$.available",
                        is(itemDtoBookingTest.getAvailable())));
    }

    @Test
    public void getItemsByUserId() throws Exception {
        List<ItemDtoBooking> items = List.of(itemDtoBookingTest);
        PageRequest.of(0, 1);

        when(itemService.getItemsByUserId(Mockito.anyLong(),
                Mockito.any())).thenReturn(items);

        ResultActions resultActions = performItemGet(ITEM_API + "?from=1&size=1");
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id",
                        is(itemDtoBookingTest.getId()), Long.class))
                .andExpect(jsonPath("$[0].name",
                        is(itemDtoBookingTest.getName())))
                .andExpect(jsonPath("$[0].description",
                        is(itemDtoBookingTest.getDescription())))
                .andExpect(jsonPath("$[0].available",
                        is(itemDtoBookingTest.getAvailable())));
    }

    @Test
    public void getItemsByQuery() throws Exception {
        List<ItemDto> items = List.of(itemDtoTest);
        PageRequest.of(0, 1);

        when(itemService.getItemsByQuery(Mockito.anyString(),
                Mockito.any())).thenReturn(items);

        ResultActions resultActions = performItemGet(ITEM_API + "/search?text=query&from=1&size=1");
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDtoTest.getId()),
                        Long.class))
                .andExpect(jsonPath("$[0].name",
                        is(itemDtoTest.getName())))
                .andExpect(jsonPath("$[0].description",
                        is(itemDtoTest.getDescription())))
                .andExpect(jsonPath("$[0].available",
                        is(itemDtoTest.getAvailable())));
    }

    @Test
    void createComment() throws Exception {
        CommentDto commentDto = CommentMapper.mapToCommentDtos(comment);

        when(itemService.createComment(Mockito.any(Comment.class),
                Mockito.anyLong(), Mockito.anyLong())).thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(comment))
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()),
                        Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }
}