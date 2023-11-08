package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;


    @InjectMocks
    private ItemServiceImpl itemService;

    private ItemDto itemDtoTest;
    private Item itemTest;

    private User userTest;

    private Booking bookingTest;

    private Comment commentTest;

    @BeforeEach
    public void setUp() {

        itemService = new ItemServiceImpl(userService, itemRepository, bookingRepository,
                commentRepository, itemRequestRepository);

        userTest = new User();
        userTest.setId(1L);
        userTest.setName("Name");
        userTest.setEmail("email@mail.ru");

        itemDtoTest = ItemDto.builder()
                .name("itemDto1")
                .description("descriptionDto1")
                .available(true)
                .build();

        itemTest = new Item();
        itemTest.setId(1L);
        itemTest.setName("item");
        itemTest.setDescription("description");
        itemTest.setAvailable(true);
        itemTest.setOwner(userTest);

        bookingTest = new Booking();
        bookingTest.setId(1L);
        bookingTest.setStart(LocalDateTime.now());
        bookingTest.setEnd(LocalDateTime.now());
        bookingTest.setItem(itemTest);
        bookingTest.setBooker(userTest);

        commentTest = new Comment();
        commentTest.setId(1L);
        commentTest.setText("commentTest");
        commentTest.setAuthor(userTest);
        commentTest.setItem(itemTest);

        UserDto userDtoTest1 = new UserDto();
        userDtoTest1.setName("NameDto1");
        userDtoTest1.setEmail("testDto1@mail.ru");
    }

    @Test
    public void createItem() {
        Item item = ItemMapper.mapToItem(itemDtoTest, userTest);
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item);

        ItemDto itemReturn = itemService.createItem(itemDtoTest, 1L);
        assertEquals(itemDtoTest.getName(), itemReturn.getName());
        assertEquals(itemDtoTest.getDescription(), itemReturn.getDescription());
        assertTrue(itemReturn.getAvailable());
    }

    @Test
    public void createItemShouldReturnErrorItemNotValidated() {
        itemDtoTest.setDescription("");
        ItemMapper.mapToItem(itemDtoTest, userTest);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.createItem(itemDtoTest, 1L));
        assertEquals("Item data not validated", ex.getMessage());
    }

    @Test
    void updateItem() {
        itemDtoTest.setId(1L);
        itemDtoTest.setName("rename");
        itemDtoTest.setDescription("redescription");
        itemDtoTest.setAvailable(false);

        Long userId = userTest.getId();
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(itemTest);

        ItemDto itemReturn = itemService.updateItem(itemDtoTest, userId);
        assertEquals("rename", itemReturn.getName());
        assertEquals("redescription", itemReturn.getDescription());
        assertFalse(itemReturn.getAvailable());
    }

    @Test
    void updateItemName() {
        itemDtoTest.setId(1L);
        itemDtoTest.setName("rename");

        Long userId = userTest.getId();
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(itemTest);

        ItemDto itemReturn = itemService.updateItem(itemDtoTest, userId);
        assertEquals("rename", itemReturn.getName());
        assertEquals(itemDtoTest.getDescription(), itemReturn.getDescription());
        assertTrue(itemReturn.getAvailable());
    }

    @Test
    void updateItemDescription() {
        itemDtoTest.setId(1L);
        itemDtoTest.setDescription("redescription");

        Long userId = userTest.getId();
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(itemTest);

        ItemDto itemReturn = itemService.updateItem(itemDtoTest, userId);
        assertEquals(itemDtoTest.getName(), itemReturn.getName());
        assertEquals("redescription", itemReturn.getDescription());
        assertTrue(itemReturn.getAvailable());
    }

    @Test
    void updateItemAvailable() {
        itemDtoTest.setId(1L);
        itemDtoTest.setAvailable(false);

        Long userId = userTest.getId();
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(itemTest);

        ItemDto itemReturn = itemService.updateItem(itemDtoTest, userId);
        assertEquals(itemDtoTest.getName(), itemReturn.getName());
        assertEquals(itemDtoTest.getDescription(), itemReturn.getDescription());
        assertFalse(itemReturn.getAvailable());
    }

    @Test
    public void updateItemShouldReturnErrorInvalidDescription() {
        itemDtoTest.setId(1L);
        itemDtoTest.setDescription("");

        Long userId = userTest.getId();
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.updateItem(itemDtoTest, userId));
        assertEquals("Item data not validated", ex.getMessage());
    }

    @Test
    public void updateItemShouldReturnErrorInvalidName() {
        itemDtoTest.setId(1L);
        itemDtoTest.setName("");

        Long userId = userTest.getId();
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.updateItem(itemDtoTest, userId));
        assertEquals("Item data not validated", ex.getMessage());
    }

    @Test
    void getItemWithBookingById() {
        Long userId = userTest.getId();

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));
        when(commentRepository.findByItemId(Mockito.anyLong()))
                .thenReturn(List.of(commentTest));
        when(bookingRepository.findByItemIdNext(Mockito.anyLong())).thenReturn(bookingTest);

        ItemDtoBooking itemReturn = itemService.getItemById(itemTest.getId(), userId);
        assertEquals(itemTest.getName(), itemReturn.getName());
        assertEquals(itemTest.getDescription(), itemReturn.getDescription());
        assertEquals(itemTest.getAvailable(), itemReturn.getAvailable());
        assertEquals(itemTest.getComments(), itemReturn.getComments());
    }

    @Test
    void getItemsByUserId() {
        List<Item> sourceItems = List.of(itemTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(itemRepository.findByOwnerIdOrderByIdAsc(Mockito.anyLong(),
                Mockito.any())).thenReturn(sourceItems);
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));
        when(commentRepository.findByItemId(Mockito.anyLong()))
                .thenReturn(List.of(commentTest));
        when(bookingRepository.findByItemIdNext(Mockito.anyLong())).thenReturn(bookingTest);

        List<ItemDtoBooking> targetItemDtos = itemService.getItemsByUserId(userTest.getId(),
                pageRequest);

        assertEquals(sourceItems.size(), targetItemDtos.size());
        for (Item sourceItem : sourceItems) {
            assertThat(targetItemDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description",
                            equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable()))
            )));
        }
    }

    @Test
    void getItemsByQuery() {
        List<Item> sourceItems = List.of(itemTest);
        String query = "query";
        PageRequest pageRequest = PageRequest.of(0, 1);

        when(itemRepository.search(Mockito.anyString(), Mockito.any()))
                .thenReturn(sourceItems);

        List<ItemDto> targetItemDtos = itemService.getItemsByQuery(query, pageRequest);

        assertEquals(sourceItems.size(), targetItemDtos.size());
        for (Item sourceItem : sourceItems) {
            assertThat(targetItemDtos, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description",
                            equalTo(sourceItem.getDescription())),
                    hasProperty("available",
                            equalTo(sourceItem.getAvailable()))
            )));
        }
    }

    @Test
    void getItemsByQueryIsEmpty() {
        List<Item> sourceItems = List.of(itemTest);
        String query = "";
        PageRequest pageRequest = PageRequest.of(0, 1);

        when(itemRepository.search(Mockito.anyString(),
                Mockito.any())).thenReturn(sourceItems);

        List<ItemDto> targetItemDtos = itemService.getItemsByQuery(query, pageRequest);
        assertTrue(targetItemDtos.isEmpty());
    }

    @Test
    void createComment() {
        when(bookingRepository.findBookingForComment(Mockito.anyLong(),
                Mockito.anyLong())).thenReturn(bookingTest);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(commentRepository.save(Mockito.any(Comment.class))).thenReturn(commentTest);

        CommentDto commentDtoReturn = itemService.createComment(commentTest, 1L, 1L);
        assertEquals(commentTest.getText(), commentDtoReturn.getText());
        assertEquals(commentTest.getAuthor().getName(), commentDtoReturn.getAuthorName());
        assertEquals(commentTest.getCreated(), commentDtoReturn.getCreated());
    }

    @Test
    void createCommentShouldReturnErrorInvalidBooking() {
        when(bookingRepository.findBookingForComment(Mockito.anyLong(),
                Mockito.anyLong())).thenReturn(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.createComment(commentTest, 1L, 1L));
        assertEquals("The user has not used the item", ex.getMessage());
    }

    @Test
    void getItemById() {
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemTest));

        Item itemReturn = itemService.getItemById(1L);
        assertEquals(itemTest.getName(), itemReturn.getName());
        assertEquals(itemTest.getDescription(), itemReturn.getDescription());
        assertEquals(itemTest.getAvailable(), itemReturn.getAvailable());
        assertEquals(itemTest.getOwner().getName(), itemReturn.getOwner().getName());
    }

    @Test
    void getItemByIdShouldReturnErrorNotExist() {
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(1L));
        assertEquals("Item with Id = 1 does not exist", ex.getMessage());
    }
}