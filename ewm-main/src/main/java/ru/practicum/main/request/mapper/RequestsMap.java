package ru.practicum.main.request.mapper;

import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.model.ParticipationRequest;
import ru.practicum.main.request.model.StatusEventRequestUpdateResult;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestsMap {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .id(participationRequest.getId())
                .created(participationRequest.getCreated().toString().replace("T", " ").substring(0, participationRequest.getCreated().toString().indexOf(".")))
                .event(participationRequest.getEventsWithRequests().getId())
                .requester(participationRequest.getRequester().getId())
                .status(participationRequest.getStatus())
                .build();
    }

    public static List<ParticipationRequestDto> mapToListParticipationRequestDto(List<ParticipationRequest> listParticipationRequest) {
        List<ParticipationRequestDto> listParticipationRequestDto = new ArrayList<>();
        for (ParticipationRequest request : listParticipationRequest) {
            listParticipationRequestDto.add(mapToParticipationRequestDto(request));
        }
        return listParticipationRequestDto;
    }

    public static EventRequestStatusUpdateResult mapToEventRequestStatusUpdateResult(List<ParticipationRequest> requestList) {
        List<ParticipationRequestDto> requestsConfirmed = new ArrayList<>();
        List<ParticipationRequestDto> requestsRejected = new ArrayList<>();

        for (ParticipationRequest request : requestList) {
            if (request.getStatus().equals(StatusEventRequestUpdateResult.CONFIRMED)) {
                requestsConfirmed.add(mapToParticipationRequestDto(request));
            }
            if (request.getStatus().equals(StatusEventRequestUpdateResult.REJECTED)) {
                requestsRejected.add(mapToParticipationRequestDto(request));
            }
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requestsConfirmed)
                .rejectedRequests(requestsRejected)
                .build();
    }
}