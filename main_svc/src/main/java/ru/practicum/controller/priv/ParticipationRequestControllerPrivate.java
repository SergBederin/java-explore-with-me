package ru.practicum.controller.priv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.service.EventService;
import ru.practicum.service.ParticipationService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@Slf4j
public class ParticipationRequestControllerPrivate {

    private final ParticipationService participationService;
    private final EventService eventService;


    @Autowired
    public ParticipationRequestControllerPrivate(ParticipationService participationService, EventService eventService) {
        this.participationService = participationService;
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto postParticipationRequest(@PathVariable(name = "userId") @Positive int userId,
                                                            @RequestParam(name = "eventId", required = true) @Positive int eventId) {
        EventFullDto eventFullDto = eventService.getEventById(eventId);
        ParticipationRequestDto requestDto = participationService.create(userId, eventFullDto);
        log.info("Выполняется запрос Post//users/{userId}/requests для создания нового запроса userid={}, eventId={},requestId={}", userId, eventId, requestDto.getId());
        return requestDto;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getParticipationRequestsByUser(@PathVariable(name = "userId") @Positive int userId) {

        List<ParticipationRequestDto> requestDtos = participationService.getRequestsByUser(userId);
        log.info("Выполняется запрос Get/users/{userId}/requests для получения списка заявок пользователя с userid={} в событиях других пользователей", userId);
        return requestDtos;
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto patchRequestCancel(@PathVariable(name = "userId") @Positive int userId,
                                                      @PathVariable(name = "requestId") @Positive int requestId) {
        ParticipationRequestDto participationRequestDto = participationService.patchRequestCancel(userId, requestId);
        log.info("Выполняется запрос Patch/users/{userId}/requests/{requestId}/cancel для отмены заявки Id={} от пользователя с userid={}", requestId, userId);
        return participationRequestDto;
    }
}
