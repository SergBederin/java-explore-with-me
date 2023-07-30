package ru.practicum.request.close;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.close.CloseEventsService;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.StatusEventRequestUpdateResult;
import ru.practicum.request.repository.RequestsRepository;
import ru.practicum.users.admin.UsersService;
import ru.practicum.users.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.request.mapper.RequestsMap.*;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class CloseRequestsService {
    @Autowired
    private RequestsRepository repository;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CloseEventsService eventsService;

    public List<ParticipationRequestDto> getRequestsByUserOtherEvents(int userId) {
        usersService.getUserById(userId);
        log.info("Выполнено  получение информации о заявках текущего пользователя id: {} на участие в чужих событиях.",userId);
        return mapToListParticipationRequestDto(repository.findParticipationRequestsByRequester_Id(userId));
    }

    public ParticipationRequestDto createRequestsByUserOtherEvents(int userId, int eventId) {
        User user = usersService.getUserById(userId);
        Event event = eventsService.getEventById(eventId);
        List<ParticipationRequestDto> requestDtoList = getRequestsByUserOtherEvents(userId);

        boolean conditionOne = false;

        if (requestDtoList != null && !requestDtoList.isEmpty()) {
            for (ParticipationRequestDto requestDto : requestDtoList) {
                if (requestDto.getEvent() == eventId) {
                    conditionOne = true;
                    break;
                }
            }
        }

        boolean conditionTwo = event.getInitiator().getId() == user.getId();
        boolean conditionThree = event.getState().equals(EventStatus.PENDING) || event.getState().equals(EventStatus.CANCELED);
        boolean conditionFour = (event.getConfirmedRequests() >= event.getParticipantLimit()) && event.getParticipantLimit() != 0;
        boolean conditionFive = !event.isRequestModeration();
        boolean conditionSix = event.getParticipantLimit() == 0;

        if (conditionOne || conditionTwo || conditionThree || conditionFour) {
            throw new ConflictException("Нарушение целостности данных.");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .eventsWithRequests(event)
                .requester(user)
                .build();

        if (conditionFive || conditionSix) {
            request.setStatus(StatusEventRequestUpdateResult.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            request.setStatus(StatusEventRequestUpdateResult.PENDING);
        }

        log.info("Выполнено  добавление запроса от текущего пользователя id: {} на участие в событии.",userId);
        return mapToParticipationRequestDto(repository.save(request));
    }

    public ParticipationRequestDto cancelRequestsByUserOtherEvents(int userId, int requestId) {
        ParticipationRequest request = repository.findParticipationRequestByIdAndRequester_Id(requestId, userId).orElseThrow(() -> new NotFoundException("Запрос не найден."));
        request.setStatus(StatusEventRequestUpdateResult.CANCELED);

        log.info("Выполнено  отмена своего запроса на участие в событии.");
        return mapToParticipationRequestDto(repository.save(request));
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUser(int userId, int eventId) {
        log.info("Выполнено  получение информации о запросах на участие в событии текущего пользователя id: {}.",userId);
        return mapToListParticipationRequestDto(repository.findParticipationRequestsByEventsWithRequests_IdAndEventsWithRequests_Initiator_Id(eventId, userId));
    }


    public EventRequestStatusUpdateResult changeStatusRequestsByUser(int userId, int eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        List<ParticipationRequest> requestList = repository.findParticipationRequestsByEventsWithRequests_IdAndEventsWithRequests_Initiator_Id(eventId, userId);

        if (requestList.isEmpty()) {
            throw new NotFoundException("Запрос не найден.");
        }

        for (ParticipationRequest request : requestList) {

            boolean conditionOne = (request.getEventsWithRequests().getParticipantLimit() == 0) || !request.getEventsWithRequests().isRequestModeration();
            boolean conditionTwo = request.getEventsWithRequests().getConfirmedRequests() >= request.getEventsWithRequests().getParticipantLimit();
            boolean conditionThree = request.getStatus().equals(StatusEventRequestUpdateResult.PENDING);

            if (conditionOne) {
                request.setStatus(StatusEventRequestUpdateResult.CONFIRMED);
                request.getEventsWithRequests().setConfirmedRequests(request.getEventsWithRequests().getConfirmedRequests() + 1);
                repository.save(request);
            }

            if (conditionThree) {
                if (conditionTwo) {
                    request.setStatus(StatusEventRequestUpdateResult.REJECTED);
                    repository.save(request);
                } else {
                    request.setStatus(eventRequestStatusUpdateRequest.getStatus());
                    if (request.getStatus().equals(StatusEventRequestUpdateResult.CONFIRMED)) {
                        request.getEventsWithRequests().setConfirmedRequests(request.getEventsWithRequests().getConfirmedRequests() + 1);
                    }
                    repository.save(request);
                }
            } else {
                throw new ConflictException("Нарушение целостности данных.");
            }
        }

        log.info("Выполнено  изменение статуса заявок на участии в событии текущего пользователя id: {}",userId);
        return mapToEventRequestStatusUpdateResult(requestList);
    }
}
