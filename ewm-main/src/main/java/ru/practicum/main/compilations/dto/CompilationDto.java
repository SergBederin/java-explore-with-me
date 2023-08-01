package ru.practicum.main.compilations.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.main.events.dto.EventShortDto;

import java.util.List;

@Data
@Builder
public class CompilationDto {
    private List<EventShortDto> events;
    private int id;
    private boolean pinned;
    private String title;
}
