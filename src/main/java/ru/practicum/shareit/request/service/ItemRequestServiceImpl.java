package ru.practicum.shareit.request.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Builder
@Service
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemService itemService;

    private final UserService userService;

    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemRequestDto createItemRequest(ItemRequestDtoIn itemRequestDtoIn, Long userId) {
        User user = userService.getUserById(userId);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDtoIn.getDescription());
        itemRequest.setRequestor(user);
        return ItemRequestMapper.mapToItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getAllByRequestorId(Long userId) {
        userService.getUserById(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return getRequests(requests);
    }

    @Override
    public List<ItemRequestDto> getAllByNotRequestorId(Long userId, Pageable pageable) {
        userService.getUserById(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pageable);
        return getRequests(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userService.getUserById(userId);

        if (!itemRequestRepository.existsById(requestId)) {
            throw new NotFoundException("Request with Id = " + requestId + " does not exist");
        }

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NotFoundException("Request with Id = " + requestId + " does not exist"));

        List<ItemDto> items = ItemMapper
                .mapToItemsDto(itemRepository.findByRequestId(itemRequest.getId()));
        itemRequest.setItems(items);

        return ItemRequestMapper.mapToItemRequestDto(itemRequest);
    }


    private List<ItemRequestDto> getRequests(List<ItemRequest> requests) {
        List<ItemRequestDto> result = new ArrayList<>();
        for (ItemRequest itemRequest : requests) {
            List<ItemDto> items = ItemMapper
                    .mapToItemsDto(itemRepository.findByRequestId(itemRequest.getId()));
            itemRequest.setItems(items);
            result.add(ItemRequestMapper.mapToItemRequestDto(itemRequest));
        }
        return result;
    }
}
