package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Map;


@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@Valid @RequestBody RequestDto requestDto,
                                                    @RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Creating item request {}, userId={}", requestDto, userId);
        return requestClient.createItemRequest(requestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByRequestorId(@RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Get item request with userId={}", userId);
        return requestClient.getAllByRequestorId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllByRequestsByUserId(@RequestHeader(USER_ID_HEADER) long userId,
                                                           @Min(0) @RequestParam(defaultValue = "0") int from,
                                                           @Min(0) @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        log.info("Get item request with userId={}, from={}, size={}", userId, from, size);
        return requestClient.getAllByNotRequestorId(userId, parameters);
    }

    @GetMapping("{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(USER_ID_HEADER) long userId,
                                                 @PathVariable long requestId) {
        log.info("Get item request with userId={}", userId);
        return requestClient.getRequestById(userId, requestId);
    }
}
