package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Integer, Item> items = new HashMap<>();

    private int itemId = 1;


    public int getNextId() {
        return itemId++;
    }

    @Override
    public Item putItem(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        log.info("item successfully added");
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        log.info("item successfully updated");
        return item;
    }

    @Override
    public Item getItemById(int itemId) {

        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Item with Id = " + itemId + " does not exist");
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

}
