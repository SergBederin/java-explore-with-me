package ru.practicum.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
public class UserControllerAdmin {
    private final UserService userService;

    @Autowired
    public UserControllerAdmin(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) //201
    public UserDto postUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        UserDto userDto = userService.createUser(newUserRequest);
        log.info("Выполняется запрос Post/admin/users для создания нового пользователя name={}, email={}", userDto.getName(), userDto.getEmail());
        return userDto;
    }

    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(name = "ids", required = false) List<Integer> ids,
                                     @RequestParam(name = "from", defaultValue = "0") Integer from,
                                     @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Выполняется запрос Get/admin/users для получения списка пользователей. ids={}, from={}, size={}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteUser(@PathVariable(name = "userId") int userId) {
        userService.deleteById(userId);
        log.info("Выполняется запрос Delete/admin/users для удаления пользователь с Id={}", userId);
    }
}
