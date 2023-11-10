package ru.practicum.shareit.item.service;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Builder
@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private static ItemMapper itemMapper;

    private final UserService userService;

    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User user = userService.getUserById(userId);
        Item item = ItemMapper.mapToItem(itemDto, user);

        ItemRequest itemRequest = getItemRequestById(itemDto.getRequestId());
        item.setRequest(itemRequest);

        validateItemConstraints(item);
        log.info("item successfully added");
        return itemMapper.mapToItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
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

        validateItemConstraints(oldItem);
        log.info("item successfully updated");
        return itemMapper.mapToItemDto(itemRepository.save(oldItem));
    }

    @Override
    @Transactional
    public ItemDtoBooking getItemById(Long itemId, Long userId) {
        userService.getUserById(userId);
        Item item = getItemById(itemId);
        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentDto> commentsDto = CommentMapper.mapToCommentsDto(comments);
        item.setComments(commentsDto);

        Booking bookingLast = bookingRepository.findByItemIdLast(itemId);
        Booking bookingNext = bookingRepository.findByItemIdNext(itemId);

        if (userId.equals(item.getOwner().getId())) {
            return ItemMapper.mapToItemDtoBooking(bookingLast, bookingNext, item);
        } else {
            return ItemMapper.mapToItemDtoBooking(null, null, item);
        }
    }


    @Override
    @Transactional
    public List<ItemDtoBooking> getItemsByUserId(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId, pageable);
        List<ItemDtoBooking> result = new ArrayList<>();

        for (Item item : items) {
            result.add(getItemById(item.getId(), userId));
        }
        return result;
    }

    @Override
    @Transactional
    public List<ItemDto> getItemsByQuery(String query, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.search(query, pageable);
        if (query.isEmpty()) {
            return new ArrayList<>();
        }
        return ItemMapper.mapToItemsDto(items);
    }

    @Override
    @Transactional
    public CommentDto createComment(Comment comment, Long itemId, Long bookerId) {
        Booking booking = bookingRepository.findBookingForComment(itemId, bookerId);
        if (booking == null) {
            throw new BadRequestException("The user has not used the item");
        }
        Item item = getItemById(itemId);
        User user = userService.getUserById(bookerId);
        comment.setItem(item);
        comment.setAuthor(user);
        return CommentMapper.mapToCommentDtos(commentRepository.save(comment));
    }


    @Override
    @Transactional
    public Item getItemById(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Item with Id = " + itemId + " does not exist");
        }
        return itemRepository.findById(itemId).get();
    }

    @Transactional
    public ItemRequest getItemRequestById(Long requestId) {
        if (requestId != null) {
            return itemRequestRepository.findById(requestId)
                    .orElseThrow(() ->
                            new NotFoundException("Request with Id = " + requestId + " does not exist"));
        }
        return null;
    }

    private void validateItemConstraints(Item oldItem) {
        Set<ConstraintViolation<Item>> violations = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .validate(oldItem);

        if (!violations.isEmpty()) {
            throw new BadRequestException("Item data not validated");
        }
    }
}
