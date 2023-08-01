package ru.practicum.main.events.close;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.close.CloseEventsService;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.NewEventDto;
import ru.practicum.main.events.dto.UpdateEventUserRequest;
import ru.practicum.main.messages.LogMessages;

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
        log.debug(LogMessages.TRY_PRIVATE_GET_EVENT_USER.label, userId);
        return service.getEventsByUser(userId, from, size);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{userId}/events")
    public EventFullDto createEvents(@PathVariable(name = "userId") int userId,
                                     @RequestBody @Valid NewEventDto newEventDto) {
        log.debug(LogMessages.TRY_PRIVATE_POST_EVENT_USER_ID.label, userId);
        return service.createEvents(userId, newEventDto);
    }

    @GetMapping(path = "/{userId}/events/{eventId}")
    public EventFullDto getEventsByUserFullInfo(@PathVariable(name = "userId") int userId,
                                                @PathVariable(name = "eventId") int eventId) {
        log.debug(LogMessages.TRY_PRIVATE_GET_EVENT_USER_ID.label, userId);
        return service.getEventsByUserFullInfo(userId, eventId);
    }

    @PatchMapping(path = "{userId}/events/{eventId}")
    public EventFullDto changeEventsByUser(@PathVariable(name = "userId") int userId,
                                           @PathVariable(name = "eventId") int eventId,
                                           @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.debug(LogMessages.TRY_PRIVATE_PATCH_EVENT_ID.label, eventId);
        return service.changeEventsByUser(userId, eventId, updateEventUserRequest);
    }
}