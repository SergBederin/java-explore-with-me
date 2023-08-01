package ru.practicum.main.events.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.dto.AdminEventRequests;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.UpdateEventAdminRequest;
import ru.practicum.main.messages.LogMessages;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminEventsController {
    @Autowired
    private AdminEventsService service;

    @GetMapping(path = "/events")
    public List<EventFullDto> findEvents(@RequestParam(name = "users", required = false) List<Integer> users,
                                         @RequestParam(name = "states", required = false) List<String> states,
                                         @RequestParam(name = "categories", required = false) List<Integer> categories,
                                         @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                         @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                         @RequestParam(name = "from", defaultValue = "0") int from,
                                         @RequestParam(name = "size", defaultValue = "10") int size) {
        log.debug(LogMessages.TRY_ADMIN_GET_EVENT.label);
        return service.findEvents(AdminEventRequests.of(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping(path = "/events/{eventId}")
    public EventFullDto changeEvents(@PathVariable(name = "eventId") int eventId,
                                     @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.debug(LogMessages.TRY_ADMIN_PATCH_EVENT_ID.label, eventId);
        return service.changeEvents(eventId, updateEventAdminRequest);
    }
}