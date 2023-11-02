package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {

    BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, Long userId);

    BookingDtoOut updateBooking(Boolean approved, Long bookingId, Long userId);

    BookingDtoOut getBookingById(Long bookingId, Long userId);

    List<BookingDtoOut> getAllByBooker(Long bookerId, String state);

    List<BookingDtoOut> getAllByOwner(Long ownerId, String state);
}
