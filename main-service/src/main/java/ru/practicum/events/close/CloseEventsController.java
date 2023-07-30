package ru.practicum.events.close;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.close.CloseEventsService;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequest;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class CloseEventsController {
    @Autowired
    private CloseEventsService service;

    @GetMapping(path = "/{userId}/events")
    public List<EventShortDto> getEventsByUser(@PathVariable(name = "userId") int userId,
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Выполняется запрос Get/{userId}/events для получения полной информации о событии, добавленном текущим пользователем id= {}.",userId);
        return service.getEventsByUser(userId, from, size);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{userId}/events")
    public EventFullDto createEvents(@PathVariable(name = "userId") int userId,
                                     @RequestBody @Valid NewEventDto newEventDto) {

        log.info("Выполняется запрос Post/{userId}/events для получения полной информации о событии, добавленном текущим пользователем id= {}.",userId);
        return service.createEvents(userId, newEventDto);
    }

    @GetMapping(path = "/{userId}/events/{eventId}")
    public EventFullDto getEventsByUserFullInfo(@PathVariable(name = "userId") int userId,
                                                @PathVariable(name = "eventId") int eventId) {
        log.info("Выполняется запрос Get/{userId}/events/{eventId} для получения событий, добавленных текущим пользователем id= {}.",userId);
        return service.getEventsByUserFullInfo(userId, eventId);
    }

    @PatchMapping(path = "{userId}/events/{eventId}")
    public EventFullDto changeEventsByUser(@PathVariable(name = "userId") int userId,
                                           @PathVariable(name = "eventId") int eventId,
                                           @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Выполняется запрос Patch/{userId}/events/{eventId} для изменения события, добавленного текущим пользователем по id= {}.",userId);
        return service.changeEventsByUser(userId, eventId, updateEventUserRequest);
    }
}
