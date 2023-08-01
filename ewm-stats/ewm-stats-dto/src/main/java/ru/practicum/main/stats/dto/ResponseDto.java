package ru.practicum.main.stats.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class ResponseDto {
    private String app;
    private String uri;
    private Long hits;
}