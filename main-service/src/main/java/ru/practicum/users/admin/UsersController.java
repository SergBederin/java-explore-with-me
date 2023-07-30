package ru.practicum.users.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.users.dto.NewUserDto;
import ru.practicum.users.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class UsersController {
    @Autowired
    private UsersService service;

    @GetMapping(path = "/users")
    public List<UserDto> getUsers(@RequestParam(name = "ids", required = false) List<Integer> ids,
                                  @RequestParam(name = "from", defaultValue = "0") int from,
                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Выполняется запрос Get/users для получения информации о пользователях.");
        return service.getUsers(ids, from, size);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/users")
    public UserDto createUsers(@RequestBody @Valid NewUserDto newUserDto) {
        log.info("Выполняется запрос Post/users для добавления нового пользователя.");
        return service.createUsers(newUserDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/users/{userId}")
    public void deleteUsers(@PathVariable(name = "userId") int userId) {
        log.info("Выполняется запрос Delete/users для удаления пользователя по id: {}.", userId);
        service.deleteUsers(userId);
    }
}
