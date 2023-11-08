package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Item itemTest;

    private User userOwner;

    private User userBooker;

    private User userTest;

    private BookingDtoIn bookingDtoInTest;

    private Booking bookingTest;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingServiceImpl(itemService, userService, bookingRepository);

        userOwner = new User();
        userOwner.setId(1L);
        userOwner.setName("Name");
        userOwner.setEmail("email@mail.ru");

        userBooker = new User();
        userBooker.setId(2L);
        userBooker.setName("booker");
        userBooker.setEmail("booker@mail.ru");

        userTest = new User();
        userTest.setId(3L);
        userTest.setName("userTest");
        userTest.setEmail("test@mail.ru");


        itemTest = new Item();
        itemTest.setId(1L);
        itemTest.setName("item");
        itemTest.setDescription("description");
        itemTest.setAvailable(true);
        itemTest.setOwner(userOwner);

        bookingDtoInTest = new BookingDtoIn();
        bookingDtoInTest.setStart(LocalDateTime.now().plusSeconds(2));
        bookingDtoInTest.setEnd(LocalDateTime.now().plusSeconds(3));
        bookingDtoInTest.setItemId(itemTest.getId());

        bookingTest = Booking.builder()
                .id(1L)
                .start(bookingDtoInTest.getStart())
                .end(bookingDtoInTest.getEnd())
                .booker(userBooker)
                .item(itemTest)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking() {
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(itemService.getItemById(Mockito.anyLong())).thenReturn(itemTest);
        when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(bookingTest);

        BookingDtoOut bookingDtoReturn = bookingService.createBooking(bookingDtoInTest, userBooker.getId());

        assertEquals(bookingDtoReturn.getStart(), bookingTest.getStart());
        assertEquals(bookingDtoReturn.getEnd(), bookingTest.getEnd());
        assertEquals(bookingDtoReturn.getItem().getId(), bookingTest.getItem().getId());
        assertEquals(bookingDtoReturn.getBooker().getName(), bookingTest.getBooker().getName());
        assertEquals(bookingDtoReturn.getStatus(), bookingTest.getStatus());
    }

    @Test
    void createBookingShouldReturnErrorBookedByOwner() {
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(itemService.getItemById(Mockito.anyLong())).thenReturn(itemTest);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoInTest, userOwner.getId()));
        assertEquals("Item is booked by the owner", ex.getMessage());
    }

    @Test
    void createBookingShouldReturnErrorBookingNotAvailable() {
        itemTest.setAvailable(false);
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(itemService.getItemById(Mockito.anyLong())).thenReturn(itemTest);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(bookingDtoInTest, userBooker.getId()));
        assertEquals("Booking not available", ex.getMessage());
    }

    @Test
    void createBookingShouldReturnErrorStartIsAfterEnd() {
        bookingDtoInTest.setStart(LocalDateTime.now().plusMinutes(10));
        bookingDtoInTest.setEnd(LocalDateTime.now().plusMinutes(9));

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(itemService.getItemById(Mockito.anyLong())).thenReturn(itemTest);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(bookingDtoInTest, userBooker.getId()));
        assertEquals("Booking start is after end", ex.getMessage());
    }

    @Test
    void createBookingShouldReturnErrorStartIsEqualEnd() {
        bookingDtoInTest.setStart(LocalDateTime.now().plusMinutes(10));
        bookingDtoInTest.setEnd(bookingDtoInTest.getStart());

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(itemService.getItemById(Mockito.anyLong())).thenReturn(itemTest);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(bookingDtoInTest, userBooker.getId()));
        assertEquals("Booking start is equal to end", ex.getMessage());
    }

    @Test
    void createBookingShouldReturnErrorNotBeenValidated() {
        bookingDtoInTest.setStart(LocalDateTime.now().minusSeconds(3));
        bookingDtoInTest.setEnd(LocalDateTime.now().minusSeconds(2));

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(itemService.getItemById(Mockito.anyLong())).thenReturn(itemTest);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(bookingDtoInTest, userBooker.getId()));
        assertEquals("Booking data has not been validated", ex.getMessage());
    }

    @Test
    void updateBookingShouldReturnApproved() {
        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(bookingTest));
        when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(bookingTest);

        BookingDtoOut bookingDtoOutReturn = bookingService.updateBooking(true, bookingTest.getId()
                , userOwner.getId());
        assertEquals(bookingTest.getStart(), bookingDtoOutReturn.getStart());
        assertEquals(bookingTest.getEnd(), bookingDtoOutReturn.getEnd());
        assertEquals(BookingStatus.APPROVED, bookingDtoOutReturn.getStatus());
    }

    @Test
    void updateBookingShouldReturnRejected() {
        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(bookingTest));
        when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(bookingTest);

        BookingDtoOut bookingDtoOutReturn = bookingService.updateBooking(false,
                bookingTest.getId(), userOwner.getId());
        assertEquals(bookingTest.getStart(), bookingDtoOutReturn.getStart());
        assertEquals(bookingTest.getEnd(), bookingDtoOutReturn.getEnd());
        assertEquals(BookingStatus.REJECTED, bookingDtoOutReturn.getStatus());
    }

    @Test
    void updateBookingShouldReturnErrorInvalidOwner() {
        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(bookingTest));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.updateBooking(true,
                        bookingTest.getId(), userBooker.getId()));
        assertEquals("Invalid owner", ex.getMessage());
    }

    @Test
    void updateBookingShouldReturnErrorRepeatedApproval() {
        bookingTest.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(bookingTest));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.updateBooking(true,
                        bookingTest.getId(), userOwner.getId()));
        assertEquals("Repeated approval", ex.getMessage());
    }

    @Test
    void getBookingByBookerId() {
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(bookingTest));

        BookingDtoOut bookingDtoOutReturn = bookingService.getBookingById(bookingTest.getId(),
                userBooker.getId());
        assertEquals(bookingTest.getStart(), bookingDtoOutReturn.getStart());
        assertEquals(bookingTest.getStatus(), bookingDtoOutReturn.getStatus());
    }

    @Test
    void getBookingByOwnerId() {
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(bookingTest));

        BookingDtoOut bookingDtoOutReturn = bookingService.getBookingById(bookingTest.getId(),
                userOwner.getId());
        assertEquals(bookingTest.getStart(), bookingDtoOutReturn.getStart());
        assertEquals(bookingTest.getStatus(), bookingDtoOutReturn.getStatus());
    }

    @Test
    void getBookingByIdShouldReturnErrorInvalidUserId() {
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userTest);
        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(bookingTest));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(bookingTest.getId(), userTest.getId()));
        assertEquals("Invalid userId", ex.getMessage());
    }

    @Test
    void getAllByBookerStateIsAll() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(bookingRepository.findByBookerIdOrderByStartDesc(Mockito.anyLong(),
                Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByBooker(userBooker.getId(),
                "ALL", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByBookerStateIsFuture() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(bookingRepository
                .findByBookerIdAndStartAfterOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByBooker(userBooker.getId(),
                "FUTURE", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByBookerStateIsPast() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(bookingRepository
                .findByBookerIdAndEndBeforeOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByBooker(userBooker.getId(),
                "PAST", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByBookerStateIsCurrent() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(bookingRepository
                .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByBooker(userBooker.getId(),
                "CURRENT", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByBookerStateIsWaiting() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(bookingRepository
                .findByBookerIdAndStatus(Mockito.anyLong(), Mockito.anyString(),
                        Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByBooker(userBooker.getId(),
                "WAITING", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByBookerStateIsRejected() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userBooker);
        when(bookingRepository
                .findByBookerIdAndStatus(Mockito.anyLong(), Mockito.anyString(),
                        Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByBooker(userBooker.getId(),
                "REJECTED", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByBookerShouldReturnErrorUnknownState() {
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.getAllByBooker(userBooker.getId(),
                        "UNKNOWN", pageRequest));
        assertEquals("Unknown state: UNKNOWN", ex.getMessage());
    }


    @Test
    void getAllByOwnerStateIsAll() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(bookingRepository.findByOwnerId(Mockito.anyLong(),
                Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByOwner(userOwner.getId(),
                "ALL", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByOwnerStateIsFuture() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(bookingRepository.findByOwnerIdFuture(Mockito.anyLong(),
                Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByOwner(userOwner.getId(),
                "FUTURE", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByOwnerStateIsPast() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(bookingRepository.findByOwnerIdPast(Mockito.anyLong(),
                Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByOwner(userOwner.getId(),
                "PAST", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByOwnerStateIsCurrent() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(bookingRepository.findByOwnerIdCurrent(Mockito.anyLong(),
                Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByOwner(userOwner.getId(),
                "CURRENT", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByOwnerStateIsWaiting() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(bookingRepository.findByOwnerIdState(Mockito.anyLong(), Mockito.anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByOwner(userOwner.getId(),
                "WAITING", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByOwnerStateIsRejected() {
        List<Booking> sourceBookings = List.of(bookingTest);
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(bookingRepository.findByOwnerIdState(Mockito.anyLong(), Mockito.anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(sourceBookings);

        List<BookingDtoOut> targetBookingsDto = bookingService.getAllByOwner(userOwner.getId(),
                "REJECTED", pageRequest);
        assertEquals(sourceBookings.size(), targetBookingsDto.size());
        for (Booking sourceBooking : sourceBookings) {
            assertThat(targetBookingsDto, hasItem(allOf(
                    hasProperty("id", equalTo(sourceBooking.getId())),
                    hasProperty("start", equalTo(sourceBooking.getStart())),
                    hasProperty("end", equalTo(sourceBooking.getEnd())),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    @Test
    void getAllByOwnerShouldReturnErrorUnknownState() {
        PageRequest pageRequest = PageRequest.of(1, 1);

        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> bookingService.getAllByOwner(userOwner.getId(), "UNKNOWN", pageRequest));
        assertEquals("Unknown state: UNKNOWN", ex.getMessage());
    }
}