package ru.practicum.dto;

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