package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.request.model.StatusEventRequestUpdateResult;

import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Integer> requestIds;
    private StatusEventRequestUpdateResult status;
}
