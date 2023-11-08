package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createItemRequest(ItemRequestDtoIn itemRequestDtoIn, Long userId);

    List<ItemRequestDto> getAllByRequestorId(Long userId);

    List<ItemRequestDto> getAllByNotRequestorId(Long userId, Pageable pageable);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
