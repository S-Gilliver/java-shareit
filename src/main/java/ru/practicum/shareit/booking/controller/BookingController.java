package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    public final BookingService bookingService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingDtoOut createBooking(@Valid @RequestBody BookingDtoIn bookingDtoIn,
                                       @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.createBooking(bookingDtoIn, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut updateBooking(@RequestParam(name = "approved") @NotNull Boolean approved,
                                       @PathVariable Long bookingId,
                                       @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.updateBooking(approved, bookingId, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut getBookingById(@PathVariable Long bookingId,
                                        @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoOut> getAllByBooker(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                              @RequestParam(required = false, defaultValue = "ALL") String state,
                                              @Min(0) @RequestParam(defaultValue = "0") int from,
                                              @Min(0) @RequestParam(defaultValue = "10") int size)  {
        return bookingService.getAllByBooker(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                             @RequestParam(required = false, defaultValue = "ALL") String state,
                                             @Min(0) @RequestParam(defaultValue = "0") int from,
                                             @Min(0) @RequestParam(defaultValue = "10") int size) {
        return bookingService.getAllByOwner(ownerId, state, from, size);
    }
}
