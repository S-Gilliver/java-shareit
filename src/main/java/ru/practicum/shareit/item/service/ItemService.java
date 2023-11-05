package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(ItemDto itemDto, Long userId);

    ItemDtoBooking getItemById(Long itemId, Long userId);

    List<ItemDtoBooking> getItemsByUserId(Long userId);

    List<ItemDto> getItemsByQuery(String query);

    CommentDto createComment(Comment comment, Long itemId, Long bookerId);

    Item getItemById(Long itemId);
}
