package ru.practicum.main.users.mapper;

import org.springframework.data.domain.Page;
import ru.practicum.main.users.dto.NewUserRequest;
import ru.practicum.main.users.dto.UserDto;
import ru.practicum.main.users.dto.UserShortDto;
import ru.practicum.main.users.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserMap {
    public static User mapToUser(NewUserRequest userRequest) {
        return User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .build();
    }

    public static UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static List<UserDto> mapToListUserDto(Page<User> listUser) {
        List<UserDto> listUserDto = new ArrayList<>();
        for (User user : listUser) {
            listUserDto.add(mapToUserDto(user));
        }
        return listUserDto;
    }

    public static List<UserDto> mapToListUserDto(List<User> listUser) {
        List<UserDto> listUserDto = new ArrayList<>();
        for (User user : listUser) {
            listUserDto.add(mapToUserDto(user));
        }
        return listUserDto;
    }

    public static UserShortDto mapToUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}