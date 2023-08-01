package ru.practicum.main.request.close;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.messages.LogMessages;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class CloseRequestsController {
    @Autowired
    private CloseRequestsService service;

    @GetMapping(path = "{userId}/requests")
    public List<ParticipationRequestDto> getRequestsByUserOtherEvents(@PathVariable(name = "userId") int userId) {
        log.debug(LogMessages.TRY_PRIVATE_GET_REQUESTS_USER_ID.label, userId);
        return service.getRequestsByUserOtherEvents(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "{userId}/requests")
    public ParticipationRequestDto createRequestsByUserOtherEvents(@PathVariable(name = "userId") int userId,
                                                                   @RequestParam(name = "eventId") int eventId) {
        log.debug(LogMessages.TRY_PRIVATE_POST_REQUESTS_USER_ID.label, userId);
        return service.createRequestsByUserOtherEvents(userId, eventId);
    }

    @PatchMapping(path = "{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequestsByUserOtherEvents(@PathVariable(name = "userId") int userId,
                                                                   @PathVariable(name = "requestId") int requestId) {
        log.debug(LogMessages.TRY_PRIVATE_PATCH_REQUESTS_USER_ID.label, userId);
        return service.cancelRequestsByUserOtherEvents(userId, requestId);
    }

    @GetMapping(path = "{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByUser(@PathVariable(name = "userId") int userId,
                                                           @PathVariable(name = "eventId") int eventId) {
        log.debug(LogMessages.TRY_PRIVATE_GET_REQUESTS_EVENT_ID.label, eventId);
        return service.getRequestsByUser(userId, eventId);
    }

    @PatchMapping(path = "{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatusRequestsByUser(@PathVariable(name = "userId") int userId,
                                                                     @PathVariable(name = "eventId") int eventId,
                                                                     @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.debug(LogMessages.TRY_PRIVATE_PATCH_REQUESTS_STATUS_EVENT_ID.label, eventId);
        return service.changeStatusRequestsByUser(userId, eventId, eventRequestStatusUpdateRequest);
    }
}