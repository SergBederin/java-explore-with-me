package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.StatusEventRequestUpdateResult;

@Data
@Builder
public class ParticipationRequestDto {
    private int id;
    private String created;
    private int event;
    private int requester;
    private StatusEventRequestUpdateResult status;
}
