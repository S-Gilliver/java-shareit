package ru.practicum.shareit.request.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryItemRequestStorage implements ItemRequestStorage {

    private final Map<Integer, ItemRequest> itemRequestMap = new HashMap<>();

    private int requestId = 1;

    private int getNextId() {
        return requestId++;
    }

    @Override
    public ItemRequest getItemRequest(int itemRequestId) {
        return itemRequestMap.get(itemRequestId);
    }

}
