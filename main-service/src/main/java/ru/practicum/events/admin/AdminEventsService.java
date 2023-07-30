package ru.practicum.events.admin;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.admin.CategoriesService;
import ru.practicum.categories.model.Category;
import ru.practicum.events.dto.AdminEventRequests;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventStatus;
import ru.practicum.events.model.QEvent;
import ru.practicum.events.repository.EventsRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidTimeException;
import ru.practicum.locations.model.Location;
import ru.practicum.locations.service.LocationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.events.mapper.EventsMap.mapToEventFullDto;
import static ru.practicum.events.mapper.EventsMap.mapToListEventFullDto;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class AdminEventsService {
    @Autowired
    private EventsRepository repository;
    @Autowired
    private CategoriesService categoriesService;
    @Autowired
    private LocationService locationService;

    @Transactional(readOnly = true)
    public List<EventFullDto> findEvents(AdminEventRequests requests) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        if (requests.hasUsers()) {
            for (Integer id : requests.getUsers()) {
                conditions.add(event.initiator.id.eq(id));
            }
        }

        if (requests.hasStates()) {
            for (Integer id : requests.getCategories()) {
                conditions.add(event.category.id.eq(id));
            }
        }

        if (requests.hasCategories()) {
            for (Integer id : requests.getCategories()) {
                conditions.add(event.category.id.eq(id));
            }
        }

        if (requests.getRangeStart() != null && requests.getRangeEnd() != null) {
            conditions.add(event.eventDate.between(requests.getRangeStart(), requests.getRangeEnd()));
        }

        PageRequest pageRequest = PageRequest.of(requests.getFrom(), requests.getSize());
        Page<Event> eventsPage;

        if (!conditions.isEmpty()) {
            BooleanExpression finalCondition = conditions.stream()
                    .reduce(BooleanExpression::and)
                    .get();

            eventsPage = repository.findAll(finalCondition, pageRequest);
        } else {
            eventsPage = repository.findAll(pageRequest);
        }
        log.info("Выполнен поиск событий");
        return mapToListEventFullDto(eventsPage);
    }

    public EventFullDto changeEvents(int eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = repository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено."));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoriesService.findCategoriesById(updateEventAdminRequest.getCategory());
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            LocalDateTime startOldDate = event.getCreatedOn();
            LocalDateTime startNewDate = LocalDateTime.parse(updateEventAdminRequest.getEventDate(), formatter);

            if (Duration.between(startOldDate, startNewDate).toMinutes() < Duration.ofHours(1).toMinutes()) {
                throw new ValidTimeException("Данные не удовлетворяет правилам создания.");
            }

            event.setEventDate(LocalDateTime.parse(updateEventAdminRequest.getEventDate(), formatter));
        }
        if (updateEventAdminRequest.getLocation() != null) {
            Location location = locationService.save(updateEventAdminRequest.getLocation());
            event.setLocation(location);
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (event.getState().equals(EventStatus.PENDING)) {
            if (updateEventAdminRequest.getStateAction() != null) {
                switch (updateEventAdminRequest.getStateAction()) {
                    case PUBLISH_EVENT:
                        event.setState(EventStatus.PUBLISHED);
                        break;
                    case REJECT_EVENT:
                        event.setState(EventStatus.CANCELED);
                        break;
                }
            }
        } else {
            throw new ConflictException("Нарушение целостности данных.");
        }

        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        log.info("Выполнено редактирование данных события и его статуса по id: {}.", eventId);
        return mapToEventFullDto(repository.save(event));
    }
}
