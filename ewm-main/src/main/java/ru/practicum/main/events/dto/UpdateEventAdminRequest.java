package ru.practicum.main.events.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.main.events.model.StateActionAdmin;
import ru.practicum.main.locations.model.Location;

@Data
@Builder
public class UpdateEventAdminRequest {
    @Length(min = 20, max = 2000)
    private String annotation;
    private Integer category;
    @Length(min = 20, max = 7000)
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateActionAdmin stateAction;
    @Length(min = 3, max = 120)
    private String title;
}