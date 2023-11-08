package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {"db.name=shareitTest"})
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager entityManager;

    private User userOwner;

    private User userRequestor;

    private User userTest;

    private ItemRequestDtoIn itemRequestDtoIn;

    @BeforeEach
    public void setUp() {
        UserDto userDtoOwner = new UserDto();
        userDtoOwner.setName("Name");
        userDtoOwner.setEmail("email@mail.ru");
        userOwner = userService.createUser(userDtoOwner);

        UserDto userDtoBooker = new UserDto();
        userDtoBooker.setName("booker");
        userDtoBooker.setEmail("booker@mail.ru");
        userRequestor = userService.createUser(userDtoBooker);

        UserDto userDtoTest = new UserDto();
        userDtoTest.setName("userTest");
        userDtoTest.setEmail("test@mail.ru");
        userTest = userService.createUser(userDtoTest);

        ItemDto itemDtoTest = ItemDto.builder()
                .name("itemDto1")
                .description("descriptionDto1")
                .available(true)
                .build();
        itemService.createItem(itemDtoTest, userOwner.getId());

        itemRequestDtoIn = ItemRequestDtoIn.builder()
                .description("request description")
                .build();
    }

    @Test
    public void createItemRequest() {
        itemRequestService.createItemRequest(itemRequestDtoIn, userRequestor.getId());

        TypedQuery<ItemRequest> query = entityManager
                .createQuery("Select i from ItemRequest i where i.description = :description",
                        ItemRequest.class);
        ItemRequest itemRequest = query
                .setParameter("description", itemRequestDtoIn.getDescription())
                .getSingleResult();

        assertEquals(itemRequestDtoIn.getDescription(), itemRequest.getDescription());
        assertNotNull(itemRequest.getId());
    }

    @Test
    void getAllByRequestorId() {
        itemRequestService.createItemRequest(itemRequestDtoIn, userRequestor.getId());

        List<ItemRequestDto> targetItemRequest = itemRequestService.getAllByRequestorId(userRequestor.getId());

        assertEquals(1, targetItemRequest.size());
        assertEquals(itemRequestDtoIn.getDescription(), targetItemRequest.get(0).getDescription());
    }

    @Test
    void getAllByRequestorIdShouldReturnEmptyList() {
        itemRequestService.createItemRequest(itemRequestDtoIn, userRequestor.getId());

        List<ItemRequestDto> targetItemRequest = itemRequestService.getAllByRequestorId(userOwner.getId());

        assertTrue(targetItemRequest.isEmpty());
    }

    @Test
    void getAllByNotRequestorId() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        itemRequestService.createItemRequest(itemRequestDtoIn, userRequestor.getId());

        List<ItemRequestDto> targetItemRequest = itemRequestService
                .getAllByNotRequestorId(userOwner.getId(), pageRequest);

        assertEquals(1, targetItemRequest.size());
        assertEquals(itemRequestDtoIn.getDescription(), targetItemRequest.get(0).getDescription());
    }

    @Test
    void getAllByNotRequestorIdShouldReturnEmptyList() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        itemRequestService.createItemRequest(itemRequestDtoIn, userRequestor.getId());

        List<ItemRequestDto> targetItemRequest = itemRequestService
                .getAllByNotRequestorId(userRequestor.getId(), pageRequest);

        assertTrue(targetItemRequest.isEmpty());
    }

    @Test
    void getRequestById() {
        Long itemRequestId = itemRequestService.createItemRequest(itemRequestDtoIn,
                userRequestor.getId()).getId();

        ItemRequestDto itemRequestReturn = itemRequestService.getRequestById(userTest.getId(), itemRequestId);

        assertEquals(itemRequestDtoIn.getDescription(), itemRequestReturn.getDescription());
    }
}