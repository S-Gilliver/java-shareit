package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, int userId);

    ItemDto updateItem(ItemDto itemDto, int userId);

    ItemDto getItemById(int itemId, int userId);

    List<ItemDto> getItemsByUserId(int userId);

    List<ItemDto> getItemsByQuery(String query);
}
