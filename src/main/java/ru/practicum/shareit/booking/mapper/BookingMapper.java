package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {

    public static BookingDtoOut mapToBookingDtoOut(Booking booking) {
        ItemDto itemDto = ItemMapper.mapToItemDto(booking.getItem());
        return BookingDtoOut.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(itemDto)
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();
    }

    public static BookingDtoForItem mapToBookingDtoForItem(Booking booking) {
        if (booking != null) {
            return BookingDtoForItem.builder()
                    .id(booking.getId())
                    .bookerId(booking.getBooker().getId())
                    .build();
        } else {
            return null;
        }
    }

    public static List<BookingDtoOut> mapToBookingsDtoOut(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::mapToBookingDtoOut)
                .collect(Collectors.toList());
    }
}
