package ru.practicum.request.close;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.close.CloseRequestsService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

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
        log.info("Выполняется запрос Get/{userId}/requests для получения информации о заявках текущего пользователя id: {} на участие в чужих событиях.",  userId);
        return service.getRequestsByUserOtherEvents(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "{userId}/requests")
    public ParticipationRequestDto createRequestsByUserOtherEvents(@PathVariable(name = "userId") int userId,
                                                                   @RequestParam(name = "eventId") int eventId) {
        log.info("Выполняется запрос Post/{userId}/requests для добавления запроса от текущего пользователя id: {} на участие в событии.",  userId);
        return service.createRequestsByUserOtherEvents(userId, eventId);
    }

    @PatchMapping(path = "{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequestsByUserOtherEvents(@PathVariable(name = "userId") int userId,
                                                                   @PathVariable(name = "requestId") int requestId) {
        log.info("Выполняется запрос Patch/{userId}/requests/{requestId}/cancel отмены своего запроса на участие в событии.");
        return service.cancelRequestsByUserOtherEvents(userId, requestId);
    }

    @GetMapping(path = "{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByUser(@PathVariable(name = "userId") int userId,
                                                           @PathVariable(name = "eventId") int eventId) {
        log.info("Выполняется запрос Get/{userId}/events/{eventId}/requests получения информации о запросах на участие в событии текущего пользователя id= {}.",userId);
        return service.getRequestsByUser(userId, eventId);
    }

    @PatchMapping(path = "{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatusRequestsByUser(@PathVariable(name = "userId") int userId,
                                                                     @PathVariable(name = "eventId") int eventId,
                                                                     @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Выполняется запрос Patch/{userId}/events/{eventId}/requests изменения статуса заявок на участии в событии текущего пользователя id= {}.",userId);
        return service.changeStatusRequestsByUser(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
