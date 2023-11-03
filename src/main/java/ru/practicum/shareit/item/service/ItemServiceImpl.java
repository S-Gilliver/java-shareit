package ru.practicum.shareit.item.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Builder
@Service
public class ItemServiceImpl implements ItemService {

    private final UserService userService;

    private static ItemMapper itemMapper;

    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User user = userService.getUserById(userId);
        Item item = ItemMapper.mapToItem(itemDto, user);

        validateItem(item);
        log.info("item successfully added");
        return itemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId) {
        User user = userService.getUserById(userId);

        Item oldItem = getItemById(itemDto.getId());

        if (!oldItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Invalid item owner");
        }

        if (itemDto.getName() != null) {
            oldItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            oldItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        oldItem.setOwner(user);

        validateItem(oldItem);
        log.info("item successfully updated");
        return itemMapper.mapToItemDto(itemRepository.save(oldItem));
    }

    @Override
    public ItemDtoBooking getItemById(Long itemId, Long userId) {
        userService.getUserById(userId);
        Item item = getItemById(itemId);
        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentDto> commentsDto = CommentMapper.mapToCommentsDto(comments);
        item.setComments(commentsDto);

        Optional<Booking> bookingLast = Optional.ofNullable(bookingRepository.findByItemIdLast(itemId));
        Optional<Booking> bookingNext = Optional.ofNullable(bookingRepository.findByItemIdNext(itemId));

        if (userId.equals(item.getOwner().getId())) {
            return ItemMapper.mapToItemDtoBooking(bookingLast, bookingNext, item);
        } else {
            return ItemMapper.mapToItemDtoBooking(Optional.empty(), Optional.empty(), item);
        }
    }


    @Override
    public List<ItemDtoBooking> getItemsByUserId(Long userId) {
        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId);
        List<ItemDtoBooking> result = new ArrayList<>();

        for (Item item : items) {
            result.add(getItemById(item.getId(), userId));
        }
        return result;
    }

    @Override
    public List<ItemDto> getItemsByQuery(String query) {
        List<Item> items = itemRepository.search(query);
        if (query.isEmpty()) {
            return new ArrayList<>();
        }
        return ItemMapper.mapToItemsDto(items);
    }

    @Override
    public CommentDto createComment(Comment comment, long itemId, long bookerId) {
        Optional<Booking> booking = Optional.ofNullable(bookingRepository.findBookingForComment(itemId, bookerId));
        if (booking.isEmpty()) {
            throw new BadRequestException("The user has not used the item");
        }
        Item item = getItemById(itemId);
        User user = userService.getUserById(bookerId);
        comment.setItem(item);
        comment.setAuthor(user);
        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public Item getItemById(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Item with Id = " + itemId + " does not exist");
        }
        return itemRepository.findById(itemId).get();
    }

    private void validateItem(Item oldItem) {
        Set<ConstraintViolation<Item>> violations = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .validate(oldItem);

        if (!violations.isEmpty()) {
            throw new BadRequestException("Item data not validated");
        }
    }
}
