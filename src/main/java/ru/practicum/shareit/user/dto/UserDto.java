package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDto {

    @NotEmpty
    private String name;

    @Email
    @NotNull
    private String email;

}