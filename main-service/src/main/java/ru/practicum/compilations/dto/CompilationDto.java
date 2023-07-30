package ru.practicum.compilations.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.events.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class CompilationDto {
    private List<EventShortDto> events;
    private int id;
    private boolean pinned;
    private String title;
}
