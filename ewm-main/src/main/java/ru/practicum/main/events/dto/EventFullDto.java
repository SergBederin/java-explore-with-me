package ru.practicum.main.events.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.events.model.EventStatus;
import ru.practicum.main.locations.model.Location;
import ru.practicum.main.users.dto.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class EventFullDto {
    private int id;
    @NotBlank
    private String annotation;
    @NotNull
    private CategoryDto category;
    private int confirmedRequests;
    private String createdOn;
    private String description;
    @NotBlank
    private String eventDate;
    @NotNull
    private UserShortDto initiator;
    @NotNull
    private Location location;
    @NotNull
    private boolean paid;
    private int participantLimit;
    private String publishedOn;
    private boolean requestModeration;
    private EventStatus state;
    @NotBlank
    private String title;
    private int views;
}