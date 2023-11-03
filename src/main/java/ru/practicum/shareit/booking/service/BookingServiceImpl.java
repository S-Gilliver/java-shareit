package ru.practicum.shareit.booking.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Builder
@Service
public class BookingServiceImpl implements BookingService {

    private final ItemService itemService;

    private final UserService userService;

    private final BookingRepository bookingRepository;

    @Override
    public BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, Long userId) {
        User user = userService.getUserById(userId);
        Item item = itemService.getItemById(bookingDtoIn.getItemId());

        validateBooking(item, userId, bookingDtoIn);

        Booking booking = Booking.builder()
                .start(bookingDtoIn.getStart())
                .end(bookingDtoIn.getEnd())
                .booker(user)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();

        validateBookingData(booking);
        return BookingMapper.mapToBookingDtoOut(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoOut updateBooking(Boolean approved, Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId);
        Item item = booking.getItem();
        User user = item.getOwner();

        if (!user.getId().equals(userId)) {
            throw new NotFoundException("Invalid owner");
        }
        if (approved && booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new BadRequestException("Repeated approval");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return BookingMapper.mapToBookingDtoOut(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoOut getBookingById(Long bookingId, Long userId) {
        userService.getUserById(userId);
        Booking booking = getBookingById(bookingId);
        User booker = booking.getBooker();
        Item item = booking.getItem();
        User owner = item.getOwner();

        if (!booker.getId().equals(userId) && !owner.getId().equals(userId)) {
            throw new NotFoundException("Invalid userId");
        }
        return BookingMapper.mapToBookingDtoOut(booking);
    }

    @Override
    public List<BookingDtoOut> getAllByBooker(Long bookerId, String state) {
        userService.getUserById(bookerId);
        if (state.equals(String.valueOf(BookingState.ALL)) || state.isEmpty()) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository
                    .findByBookerIdOrderByStartDesc(bookerId));
        } else if (state.equals(String.valueOf(BookingState.FUTURE))) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository
                    .findByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now()));
        } else if (state.equals(String.valueOf(BookingState.PAST))) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository
                    .findByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now()));
        } else if (state.equals(String.valueOf(BookingState.CURRENT))) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId,
                            LocalDateTime.now(), LocalDateTime.now()));
        } else if (state.equals(String.valueOf(BookingState.WAITING)) || state
                .equals(String.valueOf(BookingState.REJECTED))) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository
                    .findByBookerIdAndStatus(bookerId, state));
        } else {
            throw new BadRequestException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingDtoOut> getAllByOwner(Long ownerId, String state) {
        userService.getUserById(ownerId);
        if (state.equals(String.valueOf(BookingState.ALL)) || state.isEmpty()) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository.findByOwnerId(ownerId));
        } else if (state.equals(String.valueOf(BookingState.FUTURE))) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository.findByOwnerIdFuture(ownerId));
        } else if (state.equals(String.valueOf(BookingState.PAST))) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository.findByOwnerIdPast(ownerId));
        } else if (state.equals(String.valueOf(BookingState.CURRENT))) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository.findByOwnerIdCurrent(ownerId));
        } else if (state.equals(String.valueOf(BookingState.WAITING)) || state
                .equals(String.valueOf(BookingState.REJECTED))) {
            return BookingMapper.mapToBookingsDtoOut(bookingRepository.findByOwnerIdState(ownerId, state));
        } else {
            throw new BadRequestException("Unknown state: " + state);
        }
    }

    private Booking getBookingById(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new NotFoundException("Booking with Id = " + bookingId + " doesn't exist");
        }
        return bookingRepository.findById(bookingId).get();
    }

    private void validateBooking(Item item, Long userId, BookingDtoIn bookingDtoIn) {
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Item is booked by the owner");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Booking not available");
        }
        if (bookingDtoIn.getStart().isAfter(bookingDtoIn.getEnd())) {
            throw new BadRequestException("Booking start is after end");
        }
        if (bookingDtoIn.getStart().equals(bookingDtoIn.getEnd())) {
            throw new BadRequestException("Booking start is equal end");
        }
    }

    private void validateBookingData(Booking booking) {
        Set<ConstraintViolation<Booking>> violations = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .validate(booking);
        if (!violations.isEmpty()) {
            throw new BadRequestException("Booking has not been validated");
        }
    }
}
