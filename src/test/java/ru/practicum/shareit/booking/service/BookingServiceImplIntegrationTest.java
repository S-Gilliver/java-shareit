package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest
@TestPropertySource(properties = {"db.name=shareitTest"})
class BookingServiceImplIntegrationTest {

    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;
    private final EntityManager entityManager;

    private User userOwner;
    private User userBooker;
    private BookingDtoIn bookingDtoInTest;

    @Autowired
    public BookingServiceImplIntegrationTest(
            BookingService bookingService,
            ItemService itemService,
            UserService userService,
            EntityManager entityManager
    ) {
        this.bookingService = bookingService;
        this.itemService = itemService;
        this.userService = userService;
        this.entityManager = entityManager;
    }

    @BeforeEach
    public void setUp() {
        UserDto userDtoOwner = new UserDto();
        userDtoOwner.setName("Name");
        userDtoOwner.setEmail("email@mail.ru");
        userOwner = userService.createUser(userDtoOwner);

        UserDto userDtoBooker = new UserDto();
        userDtoBooker.setName("booker");
        userDtoBooker.setEmail("booker@mail.ru");
        userBooker = userService.createUser(userDtoBooker);


        ItemDto itemDtoTest = ItemDto.builder()
                .name("itemDto1")
                .description("descriptionDto1")
                .available(true)
                .build();
        ItemDto itemDto = itemService.createItem(itemDtoTest, userOwner.getId());

        bookingDtoInTest = new BookingDtoIn();
        bookingDtoInTest.setStart(LocalDateTime.now().plusSeconds(2));
        bookingDtoInTest.setEnd(LocalDateTime.now().plusSeconds(3));
        bookingDtoInTest.setItemId(itemDto.getId());
    }

    @Test
    public void createBooking() {
        bookingService.createBooking(bookingDtoInTest, userBooker.getId());

        TypedQuery<Booking> query = entityManager
                .createQuery("Select b from Booking b where b.status = :status", Booking.class);
        Booking booking = query
                .setParameter("status", BookingStatus.WAITING)
                .getSingleResult();

        assertEquals(bookingDtoInTest.getStart(), booking.getStart());
        assertEquals(bookingDtoInTest.getEnd(), booking.getEnd());
        assertNotNull(booking.getId());
    }

    @Test
    public void updateBooking() {
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoInTest, userBooker.getId());

        BookingDtoOut bookingDtoOutReturn = bookingService.updateBooking(true,
                bookingDtoOut.getId(), userOwner.getId());
        assertEquals(BookingStatus.APPROVED, bookingDtoOutReturn.getStatus());
        assertEquals(bookingDtoOut.getId(), bookingDtoOutReturn.getId());
        assertEquals(bookingDtoOut.getStart(), bookingDtoOutReturn.getStart());
    }

    @Test
    public void getBookingById() {
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoInTest, userBooker.getId());

        BookingDtoOut bookingDtoOutReturn = bookingService.getBookingById(bookingDtoOut.getId(),
                userOwner.getId());
        assertEquals(bookingDtoOut.getId(), bookingDtoOutReturn.getId());
        assertEquals(bookingDtoOut.getStart(), bookingDtoOutReturn.getStart());
    }

    @Test
    public void getAllByBooker() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoInTest, userBooker.getId());

        List<BookingDtoOut> targetBookings = bookingService.getAllByBooker(userBooker.getId(),
                "ALL", pageRequest);
        assertEquals(1, targetBookings.size());
        assertEquals(bookingDtoOut.getId(), targetBookings.get(0).getId());
        assertEquals(bookingDtoOut.getBooker().getId(), targetBookings.get(0).getBooker().getId());
    }

    @Test
    public void getAllByOwner() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        BookingDtoOut bookingDtoOut = bookingService.createBooking(bookingDtoInTest, userBooker.getId());

        List<BookingDtoOut> targetBookings = bookingService.getAllByOwner(userOwner.getId(),
                "ALL", pageRequest);
        assertEquals(1, targetBookings.size());
        assertEquals(bookingDtoOut.getId(), targetBookings.get(0).getId());
        assertEquals(bookingDtoOut.getBooker().getId(), targetBookings.get(0).getBooker().getId());
    }
}
