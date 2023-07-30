package ru.practicum.events.close;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.open.OpenCategoriesService;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserRequest;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventStatus;
import ru.practicum.events.repository.EventsRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidTimeException;
import ru.practicum.locations.service.LocationService;
import ru.practicum.users.admin.UsersService;
import ru.practicum.users.model.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.categories.mapper.CatMap.mapToCategory;
import static ru.practicum.events.mapper.EventsMap.*;
import static ru.practicum.utils.Page.paged;

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
    private UsersService usersService;
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

        log.info("Выполнено добавление нового события. {}.",newEventDto);
        return mapToEventFullDto(repository.save(mapToEvent(newEventDto, category, user)));
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventsByUserFullInfo(int userId, int eventId) {
        log.info("Выполнено получение полной информации о событии, добавленном текущим пользователем id: {}.",userId);
        return mapToEventFullDto(repository.findEventByIdAndInitiator_Id(eventId, userId).orElseThrow(() -> new NotFoundException("Событие не найдено.")));
    }

    public EventFullDto changeEventsByUser(int userId, int eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = repository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено."));

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

            log.info("Выполнено изменение события, добавленного текущим пользователем по id: {}.",eventId);
            return mapToEventFullDto(repository.save(event));
        } else {
            throw new ConflictException("Нарушение целостности данных.");
        }
    }

    public Event getEventById(int eventId) {
        return repository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено."));
    }

    private void validTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(time, formatter);

        if (Duration.between(LocalDateTime.now(), startDate).toMinutes() < Duration.ofHours(2).toMinutes()) {
            throw new ValidTimeException("Данные не удовлетворяет правилам создания.");
        }
    }
}
