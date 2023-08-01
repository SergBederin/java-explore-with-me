package ru.practicum.main.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.main.request.model.StatusEventRequestUpdateResult;

import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Integer> requestIds;
    private StatusEventRequestUpdateResult status;
}
