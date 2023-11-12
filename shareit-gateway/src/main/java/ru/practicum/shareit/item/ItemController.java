package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Map;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                             @RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Creating item {}, userId={}", itemRequestDto, userId);
        return itemClient.createItem(userId, itemRequestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemRequestDto itemRequestDto,
                                             @PathVariable long itemId,
                                             @RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Update item {}, userId={}", itemId, userId);
        return itemClient.updateItem(itemId, userId, itemRequestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable long itemId,
                                              @RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Get item {}, userId={}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUserId(@RequestHeader(USER_ID_HEADER) long userId,
                                                   @Min(0) @RequestParam(defaultValue = "0") int from,
                                                   @Min(0) @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        log.info("Get item with userId={}, from={}, size={}", userId, from, size);
        return itemClient.getItemsByUserId(userId, parameters);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByQuery(@RequestParam String text,
                                                  @Min(0) @RequestParam(defaultValue = "0") int from,
                                                  @Min(0) @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        log.info("Get item query with query={}, from={}, size={}", text, from, size);
        return itemClient.getItemsByQuery(parameters);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@Valid @RequestBody CommentRequestDto commentRequestDto,
                                                @PathVariable("itemId") long itemId,
                                                @RequestHeader(USER_ID_HEADER) long bookerId) {
        log.info("Creating comment {}, userId={}, itemId={}", commentRequestDto, bookerId, itemId);
        return itemClient.createComment(commentRequestDto, itemId, bookerId);
    }
}
