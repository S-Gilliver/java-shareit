package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Getter
@Setter
@AllArgsConstructor
public class ItemRequestDto {

    private Integer id;

    @NotBlank
    private String description;

    @Positive
    private Integer requester;

    @PastOrPresent
    private LocalDateTime created;

}
