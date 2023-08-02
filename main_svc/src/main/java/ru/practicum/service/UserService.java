package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.AlreadyExistException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.exception.PaginationParametersException;
import ru.practicum.model.User;
import ru.practicum.repository.UserJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJpaRepository userJpaRepository;

    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        String name = newUserRequest.getName();
        String newEmail = newUserRequest.getEmail();

        User testUser = userJpaRepository.findByEmail(newEmail);
        if (testUser != null) {
            throw new AlreadyExistException("Пользователь с email=" + newEmail + " уже существует");
        }
        /*добавление пользователя*/
        User user = userJpaRepository.save(UserMapper.toUser(newUserRequest));
        return UserMapper.toDto(user);
    }

    public List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size) {
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            if (from == null || size == null || from < 0 || size < 1) {
                throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
            }
            PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());
            users = userJpaRepository.findAll(page).getContent();
        } else {
            users = userJpaRepository.findAllByIdIn(ids);
        }

        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getAllUsers(List<Integer> ids) {
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        } else {
            users = userJpaRepository.findAllByIdIn(ids);
        }
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteById(int userId) {
        userJpaRepository.findById(userId)
                .orElseThrow(() -> new ElementNotFoundException("Пользователь с id= " + userId + " не найден"));
        userJpaRepository.deleteById(userId);
    }

    public UserDto getUserById(int userId) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ElementNotFoundException("Пользователь с id= " + userId + " не найден"));

        return UserMapper.toDto(user);
    }
}
