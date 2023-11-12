package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@TestPropertySource(properties = {"db.name=shareitTest"})
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private EntityManager entityManager;

    private ItemDto itemDtoTest1;

    private UserDto userDtoTest1;
    private UserDto userDtoTest2;

    private BookingDtoIn bookingDtoIn;

    private Comment commentTest;

    private static void wait(int ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @BeforeEach
    public void setUp() {
        userDtoTest1 = new UserDto();
        userDtoTest1.setName("NameDto1");
        userDtoTest1.setEmail("testDto1@mail.ru");

        userDtoTest2 = new UserDto();
        userDtoTest2.setName("NameDto2");
        userDtoTest2.setEmail("testDto2@mail.ru");

        User userTest = new User();
        userTest.setId(1L);
        userTest.setName("Name");
        userTest.setEmail("email@mail.ru");

        itemDtoTest1 = ItemDto.builder()
                .name("itemDto1")
                .description("descriptionDto1")
                .available(true)
                .build();

        Item itemTest = new Item();
        itemTest.setId(1L);
        itemTest.setName("item");
        itemTest.setDescription("description");
        itemTest.setAvailable(true);
        itemTest.setOwner(userTest);

        Booking bookingTest = new Booking();
        bookingTest.setId(1L);
        bookingTest.setStart(LocalDateTime.now());
        bookingTest.setEnd(LocalDateTime.now());
        bookingTest.setItem(itemTest);
        bookingTest.setBooker(userTest);

        bookingDtoIn = BookingDtoIn.builder()
                .start(LocalDateTime.now().plusSeconds(2))
                .end(LocalDateTime.now().plusSeconds(3))
                .build();

        commentTest = new Comment();
        commentTest.setId(1L);
        commentTest.setText("commentTest");
        commentTest.setAuthor(userTest);
        commentTest.setItem(itemTest);
    }

    @Test
    public void createItem() {
        User user = userService.createUser(userDtoTest1);
        Long userId = user.getId();

        itemService.createItem(itemDtoTest1, userId);

        TypedQuery<Item> query = entityManager.createQuery("Select i from Item i where i.name = :name",
                Item.class);
        Item item = query
                .setParameter("name", itemDtoTest1.getName())
                .getSingleResult();

        assertEquals(itemDtoTest1.getName(), item.getName());
        assertEquals(itemDtoTest1.getDescription(), item.getDescription());
        assertEquals(itemDtoTest1.getAvailable(), item.getAvailable());
        assertNotNull(item.getId());
    }

    @Test
    public void updateItem() {
        User user = userService.createUser(userDtoTest1);
        Long userId = user.getId();
        ItemDto itemDto = itemService.createItem(itemDtoTest1, userId);

        itemDto.setName("rename");
        itemDto.setDescription("redescription");
        itemDto.setAvailable(false);
        ItemDto itemDtoUpdate = itemService.updateItem(itemDto, userId);

        assertEquals("rename", itemDtoUpdate.getName());
        assertEquals("redescription", itemDtoUpdate.getDescription());
        assertFalse(itemDtoUpdate.getAvailable());
        assertEquals(itemDto.getId(), itemDtoUpdate.getId());
    }

    @Test
    public void getItemWithBookingById() {
        User user = userService.createUser(userDtoTest1);
        Long userId = user.getId();
        ItemDto itemDto = itemService.createItem(itemDtoTest1, userId);

        ItemDtoBooking itemDtoReturn = itemService.getItemById(itemDto.getId(), userId);

        assertEquals(itemDtoTest1.getName(), itemDtoReturn.getName());
        assertEquals(itemDtoTest1.getDescription(), itemDtoReturn.getDescription());
        assertEquals(itemDtoTest1.getAvailable(), itemDtoReturn.getAvailable());
        assertEquals(itemDto.getId(), itemDtoReturn.getId());
    }

    @Test
    public void getItemsByUserId() {
        User user = userService.createUser(userDtoTest1);
        Long userId = user.getId();
        ItemDto itemDto = itemService.createItem(itemDtoTest1, userId);

        List<ItemDtoBooking> targetItemDtos = itemService.getItemsByUserId(userId, 0, 1);

        assertEquals(1, targetItemDtos.size());
        assertEquals(itemDto.getId(), targetItemDtos.get(0).getId());
        assertEquals(itemDto.getName(), targetItemDtos.get(0).getName());
        assertEquals(itemDto.getDescription(), targetItemDtos.get(0).getDescription());
        assertEquals(itemDto.getAvailable(), targetItemDtos.get(0).getAvailable());
    }

    @Test
    public void getItemsByQuery() {
        User user = userService.createUser(userDtoTest1);
        Long userId = user.getId();
        ItemDto itemDto = itemService.createItem(itemDtoTest1, userId);

        List<ItemDto> targetItemDtos = itemService.getItemsByQuery(itemDto.getName(), 0, 1);
        assertEquals(1, targetItemDtos.size());
        assertEquals(itemDto.getId(), targetItemDtos.get(0).getId());
        assertEquals(itemDto.getName(), targetItemDtos.get(0).getName());
        assertEquals(itemDto.getDescription(), targetItemDtos.get(0).getDescription());
        assertEquals(itemDto.getAvailable(), targetItemDtos.get(0).getAvailable());
    }

    @Test
    public void createComment() {
        User user1 = userService.createUser(userDtoTest1);
        Long userId1 = user1.getId();
        User user2 = userService.createUser(userDtoTest2);
        Long userId2 = user2.getId();
        ItemDto itemDto = itemService.createItem(itemDtoTest1, userId1);

        bookingDtoIn.setItemId(itemDto.getId());
        bookingService.createBooking(bookingDtoIn, userId2);

        wait(3500);
        CommentDto commentDtoReturn = itemService.createComment(commentTest, itemDto.getId(),
                user2.getId());
        assertEquals(commentTest.getText(), commentDtoReturn.getText());
        assertEquals(commentTest.getCreated(), commentDtoReturn.getCreated());
        assertEquals(commentTest.getAuthor().getName(), commentDtoReturn.getAuthorName());
        assertNotNull(commentDtoReturn.getId());
    }

    @Test
    public void createCommentShouldReturnErrorNotUsed() {
        User user1 = userService.createUser(userDtoTest1);
        Long userId1 = user1.getId();
        User user2 = userService.createUser(userDtoTest2);
        Long userId2 = user2.getId();
        ItemDto itemDto = itemService.createItem(itemDtoTest1, userId1);

        bookingDtoIn.setItemId(itemDto.getId());
        bookingService.createBooking(bookingDtoIn, userId2);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.createComment(commentTest, itemDto.getId(), user2.getId()));
        assertEquals("The user has not used the item", ex.getMessage());
    }

    @Test
    public void getItemById() {
        User user = userService.createUser(userDtoTest1);
        Long userId = user.getId();
        ItemDto itemDto = itemService.createItem(itemDtoTest1, userId);

        Item itemReturn = itemService.getItemById(itemDto.getId());

        assertEquals(itemDto.getId(), itemReturn.getId());
        assertEquals(itemDto.getName(), itemReturn.getName());
        assertEquals(itemDto.getDescription(), itemReturn.getDescription());
        assertEquals(itemDto.getAvailable(), itemReturn.getAvailable());
    }

    @Test
    public void getItemByIdShouldReturnErrorNotItem() {
        User user = userService.createUser(userDtoTest1);
        Long userId = user.getId();
        itemService.createItem(itemDtoTest1, userId);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(99L));
        assertEquals("Item with Id = 99 does not exist", ex.getMessage());
    }
}