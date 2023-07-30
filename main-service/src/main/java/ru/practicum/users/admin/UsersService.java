package ru.practicum.users.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.dto.NewUserDto;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UsersRepository;

import java.util.List;

import static ru.practicum.users.mapper.UserMap.*;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class UsersService {
    @Autowired
    private UsersRepository repository;

    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Integer> ids, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());
        if (ids != null && !ids.isEmpty()) {
            log.info("Получена информация о пользователях");
            return mapToListUserDto(repository.findAllById(ids));
        } else {
            log.info("Получена информация о пользователях");
            return mapToListUserDto(repository.findAll(page));
        }
    }

    public UserDto createUsers(NewUserDto newUserRequest) {
        log.info("Добавлен новый пользователь.");
        return mapToUserDto(repository.save(mapToUser(newUserRequest)));
    }

    public void deleteUsers(int userId) {
        repository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        log.info("Пользователь с id = {} удален.", userId);
        repository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public User getUserById(int userId) {
        return repository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
    }
}
