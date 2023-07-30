package ru.practicum.compilations.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
public class NewCompilationDto {
    @UniqueElements
    private List<Integer> events;
    @Builder.Default
    private boolean pinned = false;
    @NotBlank
    @Length(min = 1, max = 50)
    private String title;
}
