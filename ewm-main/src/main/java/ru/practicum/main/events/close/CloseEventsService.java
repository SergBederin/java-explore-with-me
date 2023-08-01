package ru.practicum.main.events.close;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.categories.model.Category;
import ru.practicum.main.categories.open.OpenCategoriesService;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.NewEventDto;
import ru.practicum.main.events.dto.UpdateEventUserRequest;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.model.EventStatus;
import ru.practicum.main.events.repository.EventsRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.ValidTimeException;
import ru.practicum.main.locations.service.LocationService;
import ru.practicum.main.messages.ExceptionMessages;
import ru.practicum.main.messages.LogMessages;
import ru.practicum.main.users.admin.AdminUsersService;
import ru.practicum.main.users.model.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.main.categories.mapper.CatMap.mapToCategory;
import static ru.practicum.main.events.mapper.EventsMap.*;
import static ru.practicum.main.utils.Page.paged;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class CloseEventsService {
    @Autowired
    private EventsRepository repository;
    @Autowired
    private LocationService locationService;
    @Autowired
    private AdminUsersService usersService;
    @Autowired
    private OpenCategoriesService categoriesService;

    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUser(int userId, int from, int size) {
        Pageable page = paged(from, size);
        return mapToListEventShortDto(repository.findEventsByInitiator_Id(userId, page));
    }

    public EventFullDto createEvents(int userId, NewEventDto newEventDto) {
        validTime(newEventDto.getEventDate());

        User user = usersService.getUserById(userId);
        Category category = categoriesService.getCatById(newEventDto.getCategory());
        locationService.save(newEventDto.getLocation());

        log.debug(LogMessages.PRIVATE_POST_EVENT_USER_ID.label, userId);
        return mapToEventFullDto(repository.save(mapToEvent(newEventDto, category, user)));
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventsByUserFullInfo(int userId, int eventId) {
        log.debug(LogMessages.PRIVATE_GET_EVENT_USER.label, userId);
        return mapToEventFullDto(repository.findEventByIdAndInitiator_Id(eventId, userId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_EVENTS_EXCEPTION.label)));
    }

    public EventFullDto changeEventsByUser(int userId, int eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = repository.findById(eventId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_EVENTS_EXCEPTION.label));

        if (event.getState().equals(EventStatus.PENDING) || event.getState().equals(EventStatus.CANCELED)) {
            if (updateEventUserRequest.getEventDate() != null) {
                validTime(updateEventUserRequest.getEventDate());
                event.setEventDate(LocalDateTime.parse(updateEventUserRequest.getEventDate()));
            }
            if (updateEventUserRequest.getAnnotation() != null) {
                event.setAnnotation(updateEventUserRequest.getAnnotation());
            }
            if (updateEventUserRequest.getCategory() != null) {
                event.setCategory(mapToCategory(updateEventUserRequest.getCategory()));
            }
            if (updateEventUserRequest.getDescription() != null) {
                event.setDescription(updateEventUserRequest.getDescription());
            }
            if (updateEventUserRequest.getLocation() != null) {
                event.setLocation(updateEventUserRequest.getLocation());
            }
            if (updateEventUserRequest.getPaid() != null) {
                event.setPaid(updateEventUserRequest.getPaid());
            }
            if (updateEventUserRequest.getParticipantLimit() != null) {
                event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
            }
            if (updateEventUserRequest.getRequestModeration() != null) {
                event.setRequestModeration(updateEventUserRequest.getRequestModeration());
            }
            if (updateEventUserRequest.getStateAction() != null) {
                switch (updateEventUserRequest.getStateAction()) {
                    case SEND_TO_REVIEW:
                        event.setState(EventStatus.PENDING);
                        break;
                    case CANCEL_REVIEW:
                        event.setState(EventStatus.CANCELED);
                        break;
                }
            }
            if (updateEventUserRequest.getTitle() != null) {
                event.setTitle(updateEventUserRequest.getTitle());
            }

            log.debug(LogMessages.PRIVATE_PATCH_EVENT_ID.label, eventId);
            return mapToEventFullDto(repository.save(event));
        } else {
            throw new ConflictException(ExceptionMessages.CONFLICT_EXCEPTION.label);
        }
    }

    public Event getEventById(int eventId) {
        return repository.findById(eventId).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_EVENTS_EXCEPTION.label));
    }

    private void validTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(time, formatter);

        if (Duration.between(LocalDateTime.now(), startDate).toMinutes() < Duration.ofHours(2).toMinutes()) {
            throw new ValidTimeException(ExceptionMessages.VALID_TIME_EXCEPTION.label);
        }
    }
}