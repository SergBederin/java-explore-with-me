package ru.practicum.main.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.main.request.model.StatusEventRequestUpdateResult;

@Data
@Builder
public class ParticipationRequestDto {
    private int id;
    private String created;
    private int event;
    private int requester;
    private StatusEventRequestUpdateResult status;
}
