package ru.practicum.main.events.open;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.OpenEventRequests;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.model.EventStatus;
import ru.practicum.main.events.model.QEvent;
import ru.practicum.main.events.model.SortEvents;
import ru.practicum.main.events.repository.EventsRepository;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.ValidTimeException;
import ru.practicum.main.messages.ExceptionMessages;
import ru.practicum.main.messages.LogMessages;
import ru.practicum.main.stats.client.StatsClient;
import ru.practicum.main.stats.dto.ResponseDto;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.practicum.main.events.mapper.EventsMap.mapToEventFullDto;
import static ru.practicum.main.events.mapper.EventsMap.mapToListEventShortDto;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OpenEventsService {
    @Autowired
    private EventsRepository repository;
    private final StatsClient statsClient;
    private final Gson gson = new Gson();

    public List<EventShortDto> getEvents(OpenEventRequests requests, HttpServletRequest request) {
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        if (requests.getText() != null) {
            conditions.add(event.annotation.containsIgnoreCase(requests.getText()).or(event.description.containsIgnoreCase(requests.getText())));
        }

        if (requests.hasCategories()) {
            for (Integer id : requests.getCategories()) {
                conditions.add(event.category.id.eq(id));
            }
        }

        if (requests.getPaid() != null) {
            conditions.add(event.paid.eq(requests.getPaid()));
        }

        if (requests.getRangeStart() != null && requests.getRangeEnd() != null) {
            if (requests.getRangeStart().isAfter(requests.getRangeEnd())) {
                throw new ValidTimeException(ExceptionMessages.VALID_TIME_EXCEPTION.label);
            } else {
                conditions.add(event.eventDate.between(requests.getRangeStart(), requests.getRangeEnd()));
            }
        }

        if (requests.getOnlyAvailable() != null) {
            conditions.add(event.confirmedRequests.loe(event.participantLimit));
        }

        PageRequest pageRequest = PageRequest.of(requests.getFrom(), requests.getSize());

        if (requests.getSortEvents() != null) {
            Sort sort = makeOrderByClause(requests.getSortEvents());
            pageRequest = PageRequest.of(requests.getFrom(), requests.getSize(), sort);
        }

        conditions.add(event.state.eq(EventStatus.PUBLISHED));

        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();

        Page<Event> eventsPage = repository.findAll(finalCondition, pageRequest);

        if (eventsPage.isEmpty()) {
            throw new NotFoundException(ExceptionMessages.NOT_FOUND_EVENTS_EXCEPTION.label);
        }

        statsClient.hit(request);
        for (Event eventViewed : eventsPage) {
            eventViewed.setViews(parseViews(eventViewed, request));
        }

        repository.saveAll(eventsPage);

        log.debug(LogMessages.PUBLIC_GET_EVENT.label);
        return mapToListEventShortDto(eventsPage);
    }

    public EventFullDto getEventsById(int eventId, HttpServletRequest request) {
        Event event = repository.findEventsByIdAndStateIs(eventId, EventStatus.PUBLISHED).orElseThrow(() -> new NotFoundException(ExceptionMessages.NOT_FOUND_EVENTS_EXCEPTION.label));

        statsClient.hit(request);
        event.setViews(parseViews(event, request));

        repository.save(event);

        log.debug(LogMessages.PUBLIC_GET_EVENT_ID.label, eventId);
        return mapToEventFullDto(event);
    }

    private int parseViews(Event event, HttpServletRequest request) {
        ResponseEntity<Object> response = statsClient.stats(event.getCreatedOn().toString().replace("T", " "),
                event.getEventDate().toString().replace("T", " "),
                List.of(request.getRequestURI()),
                true);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            String body = Objects.requireNonNull(response.getBody()).toString()
                    .replace("[{", "{\"")
                    .replace("}]", "\"}")
                    .replace("=", "\":\"")
                    .replace(", ", "\",\"");
            ResponseDto responseDto = gson.fromJson(body, ResponseDto.class);
            return responseDto.getHits().intValue();
        }
        return event.getViews();
    }

    private Sort makeOrderByClause(SortEvents sortEvents) {
        switch (sortEvents) {
            case EVENT_DATE:
                return Sort.by("eventDate").descending();
            case VIEWS:
                return Sort.by("views").descending();
            default:
                return Sort.by("publishedOn").descending();
        }
    }
}