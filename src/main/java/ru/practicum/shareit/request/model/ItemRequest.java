package ru.practicum.shareit.request.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ItemRequest {
    private Integer id;

    private String description;

    private User requester;

    private LocalDateTime created;
}
