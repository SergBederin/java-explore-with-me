package ru.practicum.main.users.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserDto {
    @NotBlank
    @Email
    private String email;
    private int id;
    @NotBlank
    private String name;
}