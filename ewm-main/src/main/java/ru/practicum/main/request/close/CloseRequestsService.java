package ru.practicum.main.request.close;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.events.close.CloseEventsService;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.model.EventStatus;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.messages.ExceptionMessages;
import ru.practicum.main.messages.LogMessages;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.model.ParticipationRequest;
import ru.practicum.main.request.model.StatusEventRequestUpdateResult;
import ru.practicum.main.request.repository.RequestsRepository;
import ru.practicum.main.users.admin.AdminUsersService;
import ru.practicum.main.users.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.main.request.mapper.RequestsMap.*;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class CloseRequestsService {
    @Autowired
    private RequestsRepository repository;
    @Autowired
    private AdminUsersService usersService;
    @Autowired
    private CloseEventsService eventsService;

    public List<ParticipationRequestDto> getRequestsByUserOtherEvents(int userId) {
        usersService.getUserById(userId);

        log.debug(LogMessages.PRIVATE_GET_REQUESTS_USER_ID.label, userId);
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
            throw new ConflictException(ExceptionMessages.CONFLICT_EXCEPTION.label);
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

        log.debug(LogMessages.PRIVATE_POST_REQUESTS_USER_ID.label, userId);
        return mapToParticipationRequestDto(repository.save(request));
    }

    public ParticipationRequestDto cancelRequestsByUserOtherEvents(int userId, int requestId) {
        ParticipationRequest request = repository.findParticipationRequestByIdAndRequester_Id(requestId, userId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_REQUESTS_EXCEPTION.label));
        request.setStatus(StatusEventRequestUpdateResult.CANCELED);

        log.debug(LogMessages.PRIVATE_PATCH_REQUESTS_USER_ID.label, userId);
        return mapToParticipationRequestDto(repository.save(request));
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUser(int userId, int eventId) {
        log.debug(LogMessages.PRIVATE_GET_REQUESTS_EVENT_ID.label, eventId);
        return mapToListParticipationRequestDto(repository.findParticipationRequestsByEventsWithRequests_IdAndEventsWithRequests_Initiator_Id(eventId, userId));
    }

    public EventRequestStatusUpdateResult changeStatusRequestsByUser(int userId, int eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        List<ParticipationRequest> requestList = repository.findParticipationRequestsByEventsWithRequests_IdAndEventsWithRequests_Initiator_Id(eventId, userId);

        if (requestList.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.NOT_FOUND_REQUESTS_EXCEPTION.label);
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
                throw new ConflictException(ExceptionMessages.CONFLICT_EXCEPTION.label);
            }
        }

        log.debug(LogMessages.PRIVATE_PATCH_REQUESTS_STATUS_EVENT_ID.label, eventId);
        return mapToEventRequestStatusUpdateResult(requestList);
    }
}