package ru.practicum.shareit.request.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ItemRequest {
    private Integer id;

    private String description;

    private Integer requester;

    private LocalDateTime created;
}
