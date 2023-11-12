package ru.practicum.shareit.request.controller;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto createItemRequest(@Valid @RequestBody ItemRequestDtoIn itemRequestDtoIn,
                                            @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.createItemRequest(itemRequestDtoIn, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequestorId(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getAllByRequestorId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllByRequestsByUserId(@RequestHeader(USER_ID_HEADER) Long userId,
                                                         @Min(0) @RequestParam(defaultValue = "0") int from,
                                                         @Min(0) @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.getAllByNotRequestorId(userId, from, size);
    }

    @GetMapping("{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }

}
