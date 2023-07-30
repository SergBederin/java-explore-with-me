package ru.practicum.users.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserShortDto {
    @NotNull
    private int id;
    @NotBlank
    private String name;
}