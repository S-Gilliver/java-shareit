package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createBooking(@Valid @RequestBody BookItemRequestDto bookItemRequestDto,
                                                @RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Creating booking {}, userId={}", bookItemRequestDto, userId);
        return bookingClient.createBooking(bookItemRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> updateBooking(@RequestParam @NotNull Boolean approved,
                                                @PathVariable long bookingId,
                                                @RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Update booking {}, userId={}, boolean={}", bookingId, userId, approved);
        return bookingClient.updateBooking(approved, bookingId, userId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable long bookingId,
                                                 @RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(@RequestHeader(USER_ID_HEADER) long bookerId,
                                                 @RequestParam(required = false, defaultValue = "ALL") String state,
                                                 @Min(0) @RequestParam(defaultValue = "0") int from,
                                                 @Min(0) @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        log.info("Get booking with state {}, userId={}, from={}, size={}", state, bookerId, from, size);
        return bookingClient.getAllByBooker(bookerId, parameters);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(USER_ID_HEADER) long ownerId,
                                                @RequestParam(required = false, defaultValue = "ALL") String state,
                                                @Min(0) @RequestParam(defaultValue = "0") int from,
                                                @Min(0) @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        log.info("Get booking owner with state {}, userId={}, from={}, size={}", state, ownerId, from, size);
        return bookingClient.getAllByOwner(ownerId, parameters);
    }
}
