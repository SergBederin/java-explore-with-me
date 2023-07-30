package ru.practicum.events.mapper;

import org.springframework.data.domain.Page;
import ru.practicum.categories.model.Category;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventStatus;
import ru.practicum.users.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.categories.mapper.CatMap.mapToCategoryDto;
import static ru.practicum.users.mapper.UserMap.mapToUserShortDto;

public class EventsMap {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event mapToEvent(NewEventDto newEventDto, Category category, User user) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .confirmedRequests(0)
                .createdOn(LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter))
                .description(newEventDto.getDescription())
                .eventDate(LocalDateTime.parse(newEventDto.getEventDate(), formatter))
                .initiator(user)
                .location(newEventDto.getLocation())
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .publishedOn(LocalDateTime.now())
                .requestModeration(newEventDto.isRequestModeration())
                .state(EventStatus.PENDING)
                .title(newEventDto.getTitle())
                .views(0)
                .build();
    }

    public static EventFullDto mapToEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(mapToCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn().toString().replace("T", " "))
                .description(event.getDescription())
                .eventDate(event.getEventDate().toString().replace("T", " "))
                .initiator(mapToUserShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn().toString().replace("T", " "))
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static List<EventFullDto> mapToListEventFullDto(List<Event> listEvent) {
        List<EventFullDto> listEventFullDto = new ArrayList<>();
        for (Event event : listEvent) {
            listEventFullDto.add(mapToEventFullDto(event));
        }

        return listEventFullDto;
    }

    public static List<EventFullDto> mapToListEventFullDto(Page<Event> listEvent) {
        List<EventFullDto> listEventFullDto = new ArrayList<>();
        for (Event event : listEvent) {
            listEventFullDto.add(mapToEventFullDto(event));
        }

        return listEventFullDto;
    }

    public static EventShortDto mapToEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(mapToCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate().toString().replace("T", " "))
                .initiator(mapToUserShortDto(event.getInitiator()))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static List<EventShortDto> mapToListEventShortDto(Page<Event> listEvent) {
        List<EventShortDto> listEventShortDto = new ArrayList<>();
        for (Event event : listEvent) {
            listEventShortDto.add(mapToEventShortDto(event));
        }
        return listEventShortDto;
    }

    public static List<EventShortDto> mapToListEventShortDto(List<Event> listEvent) {
        List<EventShortDto> listEventShortDto = new ArrayList<>();
        for (Event event : listEvent) {
            listEventShortDto.add(mapToEventShortDto(event));
        }
        return listEventShortDto;
    }
}
