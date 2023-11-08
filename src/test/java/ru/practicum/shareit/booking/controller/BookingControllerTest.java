package ru.practicum.shareit.booking.controller;

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private BookingDtoIn bookingDtoIn;

    private BookingDtoOut bookingDtoOut;

    private static final String BOOKING_API = "/bookings";
    private static final String OWNER_BOOKING_API = "/bookings/owner";

    public ResultActions performBookingGet(String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .get(api)
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    public ResultActions performBookingPost(BookingDtoIn bookingDtoIn, String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                        .post(api)
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public ResultActions performBookingPatch(BookingDtoIn bookingDtoIn, String api) throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .patch(api)
                .content(mapper.writeValueAsString(bookingDtoIn))
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    @BeforeEach
    public void setUp() {
        bookingDtoIn = new BookingDtoIn();
        bookingDtoIn.setStart(LocalDateTime.now().plusSeconds(2));
        bookingDtoIn.setEnd(LocalDateTime.now().plusSeconds(3));
        bookingDtoIn.setItemId(1L);

        bookingDtoOut = BookingDtoOut.builder()
                .start(bookingDtoIn.getStart().plusSeconds(2))
                .end(bookingDtoIn.getEnd().plusSeconds(3))
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    public void createBooking() throws Exception {
        when(bookingService.createBooking(Mockito.any(BookingDtoIn.class),
                Mockito.anyLong())).thenReturn(bookingDtoOut);

        ResultActions resultActions = performBookingPost(bookingDtoIn, BOOKING_API);

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        is(String.valueOf(BookingStatus.WAITING))));
    }

    @Test
    public void createBookingShouldReturnBadRequest() throws Exception {
        bookingDtoIn.setStart(LocalDateTime.now().minusSeconds(10));

        ResultActions resultActions = performBookingPost(bookingDtoIn, BOOKING_API);
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void updateBooking() throws Exception {
        bookingDtoOut.setStatus(BookingStatus.APPROVED);
        when(bookingService.updateBooking(Mockito.any(), Mockito.anyLong(),
                Mockito.anyLong())).thenReturn(bookingDtoOut);

        ResultActions resultActions = performBookingPatch(bookingDtoIn, BOOKING_API +"/1?approved=true");

       resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        is(String.valueOf(BookingStatus.APPROVED))));
    }

    @Test
    public void getBookingById() throws Exception {
        when(bookingService.getBookingById(Mockito.anyLong(),
                Mockito.anyLong())).thenReturn(bookingDtoOut);

        ResultActions resultActions = performBookingGet(BOOKING_API + "/1");

        resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        is(String.valueOf(BookingStatus.WAITING))));
    }


    @Test
    public void getAllByBookerStateIsAll() throws Exception {
        List<BookingDtoOut> bookings = List.of(bookingDtoOut);
        PageRequest.of(0, 1);
        when(bookingService.getAllByBooker(Mockito.anyLong(), anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(bookings);

        ResultActions resultActions = performBookingGet(BOOKING_API +"?state=ALL");

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status",
                        is(String.valueOf(BookingStatus.WAITING))));
    }


    @Test
    public void getAllByBookerStateIsEmpty() throws Exception {
        List<BookingDtoOut> bookings = List.of(bookingDtoOut);
        PageRequest.of(0, 1);
        when(bookingService.getAllByBooker(Mockito.anyLong(), anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(bookings);

        ResultActions resultActions = performBookingGet(BOOKING_API);

        resultActions.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status",
                        is(String.valueOf(BookingStatus.WAITING))));
    }

    @Test
    public void getAllByBookerWithFromAndSize() throws Exception {
        List<BookingDtoOut> bookings = List.of(bookingDtoOut);
        PageRequest.of(0, 1);
        when(bookingService.getAllByBooker(Mockito.anyLong(), Mockito.anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(bookings);

        ResultActions resultActions = performBookingGet(BOOKING_API + "?from=0&size=1");

        resultActions.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status",
                        is(String.valueOf(BookingStatus.WAITING))));
    }

    @Test
    public void getAllByOwnerIsAll() throws Exception {
        List<BookingDtoOut> bookings = List.of(bookingDtoOut);
        PageRequest.of(0, 1);
        when(bookingService.getAllByOwner(Mockito.anyLong(), Mockito.anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(bookings);

        ResultActions resultActions = performBookingGet(OWNER_BOOKING_API +"?state=ALL");

        resultActions
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status",
                        is(String.valueOf(BookingStatus.WAITING))));
    }


    @Test
    public void getAllByOwnerIsEmpty() throws Exception {
        List<BookingDtoOut> bookings = List.of(bookingDtoOut);
        PageRequest.of(0, 1);

        when(bookingService.getAllByOwner(Mockito.anyLong(), anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(bookings);

        ResultActions resultActions = performBookingGet(OWNER_BOOKING_API);

        resultActions
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status",
                        is(String.valueOf(BookingStatus.WAITING))));
    }

    @Test
    public void getAllByOwnerWithFromAndSize() throws Exception {
        List<BookingDtoOut> bookings = List.of(bookingDtoOut);
        PageRequest.of(0, 1);

        when(bookingService.getAllByOwner(Mockito.anyLong(), anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(bookings);

        ResultActions resultActions = performBookingGet(OWNER_BOOKING_API + "?from=0&size=1");

        resultActions.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id",
                        is(bookingDtoIn.getId()), Long.class))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status",
                        is(String.valueOf(BookingStatus.WAITING))));
    }
}