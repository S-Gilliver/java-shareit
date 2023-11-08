package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
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

    List<ItemDtoBooking> getItemsByUserId(Long userId, Pageable pageable);

    List<ItemDto> getItemsByQuery(String query, Pageable pageable);

    CommentDto createComment(Comment comment, Long itemId, Long bookerId);

    Item getItemById(Long itemId);
}
