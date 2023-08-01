package ru.practicum.main.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.main.locations.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank
    @Length(min = 20, max = 2000)
    private String annotation;
    @NotNull
    private int category;
    @NotBlank
    @Length(min = 20, max = 7000)
    private String description;
    @NotBlank
    private String eventDate;
    @NotNull
    private Location location;
    @Builder.Default
    private boolean paid = false;
    @Builder.Default
    private int participantLimit = 0;
    @Builder.Default
    private boolean requestModeration = true;
    @NotBlank
    @Length(min = 3, max = 120)
    private String title;
}