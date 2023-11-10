package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoIn;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private ItemRequestDtoIn itemRequestDtoIn;

    private ItemRequest itemRequestTest;

    private User userOwner;

    private User userRequestor;

    private Item itemTest;

    @BeforeEach
    public void setUp() {
        itemRequestService = new ItemRequestServiceImpl(
                itemService, userService, itemRepository, bookingRepository, itemRequestRepository);

        itemRequestDtoIn = ItemRequestDtoIn.builder()
                .id(1L)
                .description("request description")
                .build();

        userOwner = new User();
        userOwner.setId(1L);
        userOwner.setName("Name");
        userOwner.setEmail("email@mail.ru");

        userRequestor = User.builder()
                .id(2L)
                .name("Name")
                .email("email@mail.ru")
                .build();

        itemRequestTest = ItemRequest.builder()
                .id(itemRequestDtoIn.getId())
                .description(itemRequestDtoIn.getDescription())
                .requestor(userRequestor)
                .created(LocalDateTime.now())
                .build();

        itemTest = new Item();
        itemTest.setId(1L);
        itemTest.setName("item");
        itemTest.setDescription("description");
        itemTest.setAvailable(true);
        itemTest.setOwner(userOwner);
    }

    @Test
    public void createItemRequest() {
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userRequestor);
        when(itemRequestRepository.save(Mockito.any(ItemRequest.class)))
                .thenReturn(itemRequestTest);

        ItemRequestDto itemRequestReturn = itemRequestService.createItemRequest(itemRequestDtoIn,
                userRequestor.getId());

        assertEquals(itemRequestTest.getDescription(), itemRequestReturn.getDescription());
        assertEquals(itemRequestTest.getCreated(), itemRequestReturn.getCreated());
    }

    @Test
    public void getAllByRequestorId() {
        List<ItemRequest> sourceItemRequest = List.of(itemRequestTest);
        List<Item> items = List.of(itemTest);
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userRequestor);
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(Mockito.anyLong()))
                .thenReturn(sourceItemRequest);
        when(itemRepository.findByRequestId(Mockito.anyLong())).thenReturn(items);

        List<ItemRequestDto> targetItemRequest = itemRequestService.getAllByRequestorId(userRequestor.getId());

        assertEquals(sourceItemRequest.size(), targetItemRequest.size());
        for (ItemRequest itemRequest : sourceItemRequest) {
            assertThat(targetItemRequest, hasItem(allOf(
                    hasProperty("id", equalTo(itemRequest.getId())),
                    hasProperty("description",
                            equalTo(itemRequest.getDescription())),
                    hasProperty("created", equalTo(itemRequest.getCreated()))
            )));
        }
    }

    @Test
    public void getAllByNotRequestorId() {
        List<ItemRequest> sourceItemRequest = List.of(itemRequestTest);
        List<Item> items = List.of(itemTest);
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(Mockito.anyLong(),
                Mockito.any(PageRequest.class))).thenReturn(sourceItemRequest);
        when(itemRepository.findByRequestId(Mockito.anyLong())).thenReturn(items);

        List<ItemRequestDto> targetItemRequest = itemRequestService
                .getAllByNotRequestorId(userRequestor.getId(), 1, 1);

        assertEquals(sourceItemRequest.size(), targetItemRequest.size());
        for (ItemRequest itemRequest : sourceItemRequest) {
            assertThat(targetItemRequest, hasItem(allOf(
                    hasProperty("id", equalTo(itemRequest.getId())),
                    hasProperty("description",
                            equalTo(itemRequest.getDescription())),
                    hasProperty("created",
                            equalTo(itemRequest.getCreated()))
            )));
        }
    }

    @Test
    public void getRequestById() {
        List<Item> items = List.of(itemTest);
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(itemRequestRepository.existsById(Mockito.anyLong())).thenReturn(true);

        when(itemRequestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(itemRequestTest));
        when(itemRepository.findByRequestId(Mockito.anyLong())).thenReturn(items);

        ItemRequestDto itemRequestReturn = itemRequestService.getRequestById(userOwner.getId(),
                itemRequestTest.getId());

        assertEquals(itemRequestTest.getDescription(), itemRequestReturn.getDescription());
        assertEquals(itemRequestTest.getCreated(), itemRequestReturn.getCreated());
    }

    @Test
    public void getRequestByIdShouldReturnErrorRequestNotExist() {
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userOwner);
        when(itemRequestRepository.existsById(Mockito.anyLong())).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(userOwner.getId(),
                        itemRequestTest.getId()));
        assertEquals("Request with Id = 1 does not exist", ex.getMessage());
    }
}