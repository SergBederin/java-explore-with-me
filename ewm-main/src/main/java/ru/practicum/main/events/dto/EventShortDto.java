package ru.practicum.main.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.users.dto.UserShortDto;

@Data
@Builder
public class EventShortDto {
    private String annotation;
    private CategoryDto category;
    private int confirmedRequests;
    private String eventDate;
    private int id;
    private UserShortDto initiator;
    private boolean paid;
    private String title;
    private int views;
}