package ru.practicum.main.users.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.messages.ExceptionMessages;
import ru.practicum.main.messages.LogMessages;
import ru.practicum.main.users.dto.NewUserRequest;
import ru.practicum.main.users.dto.UserDto;
import ru.practicum.main.users.model.User;
import ru.practicum.main.users.repository.UsersRepository;

import java.util.List;

import static ru.practicum.main.users.mapper.UserMap.*;
import static ru.practicum.main.utils.Page.paged;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class AdminUsersService {
    @Autowired
    private UsersRepository repository;

    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Integer> ids, int from, int size) {
        Pageable page = paged(from, size);
        if (ids != null && !ids.isEmpty()) {
            log.debug(LogMessages.ADMIN_GET_USERS.label);
            return mapToListUserDto(repository.findAllById(ids));
        } else {
            log.debug(LogMessages.ADMIN_GET_USERS.label);
            return mapToListUserDto(repository.findAll(page));
        }
    }

    public UserDto createUsers(NewUserRequest newUserRequest) {
        log.debug(LogMessages.ADMIN_POST_USERS.label);
        return mapToUserDto(repository.save(mapToUser(newUserRequest)));
    }

    public void deleteUsers(int userId) {
        repository.findById(userId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_USERS_EXCEPTION.label));

        log.debug(LogMessages.ADMIN_DELETE_USERS.label, userId);
        repository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public User getUserById(int userId) {
        return repository.findById(userId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_USERS_EXCEPTION.label));
    }
}