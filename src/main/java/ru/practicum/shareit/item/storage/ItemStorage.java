package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item putItem(Item item);

    Item updateItem(Item item);

    Item getItemById(int itemId);

    List<Item> getItems();

    List<ItemDto> findItemsByUserId(int userId);
}