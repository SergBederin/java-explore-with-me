package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.categoty.CategoryMapper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.dto.participationRequest.UpdateRequestState;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.CreateConditionException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.*;
import ru.practicum.repository.EventJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
@RequiredArgsConstructor
public class EventService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventJpaRepository eventJpaRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ParticipationService participationService;
    private final EntityManager entityManager;


    @Transactional
    public EventFullDto createEvent(NewEventDto newEventDto, int userId) {
        LocalDateTime newEventDateTime = LocalDateTime.parse(newEventDto.getEventDate(), TIME_FORMAT);
        if (HOURS.between(LocalDateTime.now(), newEventDateTime) < 2) {
            throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
        }

        Category category = CategoryMapper.toCategory(categoryService.getCategoryById(newEventDto.getCategory()));
        User user = UserMapper.toUser(userService.getUserById(userId));

        Event event = EventMapper.toEvent(newEventDto, category, user);
        Event savedEvent = eventJpaRepository.save(event);

        return EventMapper.toFullDto(savedEvent, 0);
    }

    public List<EventShortDto> getEventsByCategory(int catId) {
        if (catId <= 0) {
            throw new BadParameterException("Id категории должен быть >0");
        }
        List<Event> events = eventJpaRepository.findByCategoryId(catId);
        if (events.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream().map(Event::getId).collect(Collectors.toList()));

        return events.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public List<EventShortDto> getAllByUser(int userId, int from, int size) {

        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending());

        List<Event> events = eventJpaRepository.getAllByUser(userId, page);
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream()
                .map(Event::getId)
                .collect(Collectors.toList()));

        return events.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public EventFullDto getByUserAndId(int userId, int eventId) {

        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId);
        if (event == null) {
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId()));

        return EventMapper.toFullDto(event, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    public EventFullDto getEventById(int eventId) {
        Event event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ElementNotFoundException("События с id=" + eventId + " не найдено"));

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId()));
        return EventMapper.toFullDto(event, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    public EventFullDto getEventByIdWithStats(int eventId, HttpServletRequest request) {
        EventFullDto eventDto = this.getEventById(eventId);
        if (eventDto.getState() != EventState.PUBLISHED) {
            throw new ElementNotFoundException("Событие с id=" + eventId + " не опубликовано");
        }

        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-event-service");
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
        endpointHitDto.setUri(request.getRequestURI());

        StatsClient.postHit(endpointHitDto);

        return eventDto;
    }

    @Transactional
    public EventFullDto patchEvent(int userId, int eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId);
        if (event == null) {
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new DataConflictException("Нельзя обновлять событие в состоянии 'Опубликовано'");
        }

        String annotation = updateRequest.getAnnotation();
        if (!(annotation == null || annotation.isBlank())) {
            event.setAnnotation(annotation);
        }
        Integer categoryId = updateRequest.getCategory();
        if (categoryId != null && categoryId > 0) {
            CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
            if (categoryDto != null) {
                event.setCategory(CategoryMapper.toCategory(categoryDto));
            }
        }

        String newDateString = updateRequest.getEventDate();
        if (!(newDateString == null || newDateString.isBlank())) {
            LocalDateTime newDate = LocalDateTime.parse(newDateString, TIME_FORMAT);
            if (HOURS.between(LocalDateTime.now(), newDate) < 2) {
                throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
            }
            event.setEventDate(newDate);
        }
        Location location = updateRequest.getLocation();
        if (location != null) {
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        String stateString = updateRequest.getStateAction();
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionUser.valueOf(stateString)) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }
        String title = updateRequest.getTitle();
        if (!(title == null || title.isBlank())) {
            event.setTitle(title);
        }

        eventJpaRepository.save(event);
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId()));

        Event updatedEvent = eventJpaRepository.findById(event.getId())
                .orElseThrow(() -> new ElementNotFoundException("Событие с id=" + event.getId() + " не найден"));

        return EventMapper.toFullDto(updatedEvent, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    @Transactional
    public EventFullDto patchAdminEvent(int eventId, UpdateEventAdminRequest adminRequest) {
        Event event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ElementNotFoundException("События с id=" + eventId + " не найдено"));

        String annotation = adminRequest.getAnnotation();
        if (!(annotation == null || annotation.isBlank())) {
            event.setAnnotation(annotation);
        }
        int categoryId = adminRequest.getCategory();
        if (categoryId > 0) {
            CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
            if (categoryDto != null) {
                event.setCategory(CategoryMapper.toCategory(categoryDto));
            }
        }
        String description = adminRequest.getDescription();
        if (!(description == null || description.isBlank())) {
            event.setDescription(description);
        }

        String newDateString = adminRequest.getEventDate();
        if (!(newDateString == null || newDateString.isBlank())) {
            LocalDateTime newDate = LocalDateTime.parse(newDateString, TIME_FORMAT);
            if (HOURS.between(LocalDateTime.now(), newDate) < 2) {
                throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
            }
            event.setEventDate(newDate);
        }
        Location location = adminRequest.getLocation();
        if (location != null) {
            event.setLocation(location);
        }
        if (adminRequest.getPaid() != null) {
            event.setPaid(adminRequest.getPaid());
        }
        if (adminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(adminRequest.getParticipantLimit());
        }
        if (adminRequest.getRequestModeration() != null) {
            event.setRequestModeration(adminRequest.getRequestModeration());
        }

        String stateString = adminRequest.getStateAction();
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionAdmin.valueOf(stateString)) {
                case PUBLISH_EVENT:
                    if (HOURS.between(LocalDateTime.now(), event.getEventDate()) < 1) {
                        throw new CreateConditionException("Начало события должно быть минимум на один час позже момента публикации");
                    }
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new DataConflictException("Попытка опубликовать событие с id=" + event.getId() + ", которое уже опубликовано.");
                    }
                    if (event.getState() == EventState.CANCELED) {
                        throw new DataConflictException("Попытка опубликовать событие с id=" + event.getId() + ", которое уже отменено.");
                    }
                    event.setState(EventState.PUBLISHED);
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new DataConflictException("Попытка отменить событие с id=" + event.getId() + ", которое уже опубликовано.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        String title = adminRequest.getTitle();
        if (!(title == null || title.isBlank())) {
            event.setTitle(title);
        }

        eventJpaRepository.save(event);
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId()));

        Event updatedEvent = eventJpaRepository.findById(event.getId())
                .orElseThrow(() -> new ElementNotFoundException("Событие с id=" + event.getId() + " не найден"));
        return EventMapper.toFullDto(updatedEvent, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    @Transactional
    public List<ParticipationRequestDto> getParticipationInfo(int userId, int eventId) {

        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId);
        if (event == null) {
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        return participationService.getAllRequestsEventId(event.getId());
    }


    @Transactional
    public EventRequestStatusUpdateResult updateStatus(int userId, int eventId, EventRequestStatusUpdateRequest updateRequest) {

        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId);
        if (event == null) {
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        List<ParticipationRequestDto> requests = participationService.getAllRequestsEventId(eventId);
        int limit = event.getParticipantLimit();

        if (updateRequest.getStatus() == UpdateRequestState.REJECTED) {
            return rejectRequests(event, requests, updateRequest);
        } else {
            if ((limit == 0 || !event.isRequestModeration())) {
                return confirmAllRequests(event, requests, updateRequest);
            } else {
                return confirmRequests(event, requests, updateRequest);
            }
        }
    }

    @Transactional
    public List<EventFullDto> searchEvents(List<Integer> users, List<String> states, List<Integer> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        criteriaQuery = criteriaQuery.select(eventRoot);

        List<Event> resultEvents;
        Predicate complexPredicate = null;
        if (rangeStart != null && rangeEnd != null) {
            complexPredicate
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), rangeStart, rangeEnd);
        }
        if (users != null && !users.isEmpty()) {
            /*строим предикат по инициатору событий*/
            Predicate predicateForUsersId
                    = eventRoot.get("initiator").get("id").in(users);
            if (complexPredicate == null) {
                complexPredicate = predicateForUsersId;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForUsersId);
            }
        }
        if (categories != null && !categories.isEmpty()) {
            /*строим предикат по категории событий*/
            Predicate predicateForCategoryId
                    = eventRoot.get("category").get("id").in(categories);
            if (complexPredicate == null) {
                complexPredicate = predicateForCategoryId;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForCategoryId);
            }
        }
        if (states != null && !states.isEmpty()) {
            Predicate predicateForStates
                    = eventRoot.get("state").as(String.class).in(states);
            if (complexPredicate == null) {
                complexPredicate = predicateForStates;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForStates);
            }
        }
        if (complexPredicate != null) {
            criteriaQuery.where(complexPredicate);
        }
        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        resultEvents = typedQuery.getResultList();

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(resultEvents.stream().map(Event::getId).collect(Collectors.toList()));
        return resultEvents.stream()
                .map(e -> EventMapper.toFullDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EventShortDto> searchEventsWithStats(String text, List<Integer> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        criteriaQuery.select(eventRoot);

        List<Event> resultEvents;
        Predicate complexPredicate;

        if (rangeStart != null && rangeEnd != null) {
            complexPredicate
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), rangeStart, rangeEnd);
        } else {
            complexPredicate
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), LocalDateTime.now(), LocalDateTime.of(9999, 1, 1, 1, 1, 1));
        }

        if (text != null && !text.isBlank()) {
            String decodeText = URLDecoder.decode(text, StandardCharsets.UTF_8);

            Expression<String> annotationLowerCase = criteriaBuilder.lower(eventRoot.get("annotation"));
            Expression<String> descriptionLowerCase = criteriaBuilder.lower(eventRoot.get("description"));
            Predicate predicateForAnnotation
                    = criteriaBuilder.like(annotationLowerCase, "%" + decodeText.toLowerCase() + "%");
            Predicate predicateForDescription
                    = criteriaBuilder.like(descriptionLowerCase, "%" + decodeText.toLowerCase() + "%");
            Predicate predicateForText = criteriaBuilder.or(predicateForAnnotation, predicateForDescription);
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForText);
        }

        if (categories != null && !categories.isEmpty()) {
            if (categories.stream().anyMatch(c -> c <= 0)) {
                throw new BadParameterException("Id категории должен быть > 0");
            }
            Predicate predicateForCategoryId
                    = eventRoot.get("category").get("id").in(categories);
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForCategoryId);
        }

        if (paid != null) {
            Predicate predicateForPaid
                    = criteriaBuilder.equal(eventRoot.get("paid"), paid);
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForPaid);

        }

        if (onlyAvailable != null) {
            Predicate predicateForOnlyAvailable
                    = criteriaBuilder.lt(eventRoot.get("confirmedRequests"), eventRoot.get("participantLimit"));
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForOnlyAvailable);
        }

        Predicate predicateForPublished
                = criteriaBuilder.equal(eventRoot.get("state"), EventState.PUBLISHED);
        complexPredicate = criteriaBuilder.and(complexPredicate, predicateForPublished);

        criteriaQuery.where(complexPredicate);

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        resultEvents = typedQuery.getResultList();

        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-event-service");
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
        endpointHitDto.setUri(request.getRequestURI());

        StatsClient.postHit(endpointHitDto);

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(resultEvents.stream().map(Event::getId).collect(Collectors.toList()));

        Comparator<EventShortDto> comparator;
        if (sort != null && sort.equals("EVENT_DATE")) {
            comparator = Comparator.comparing(e -> LocalDateTime.parse(e.getEventDate(), TIME_FORMAT));
        } else {
            comparator = Comparator.comparing(EventShortDto::getViews);
        }
        return resultEvents.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public Set<EventFullDto> getEventsByIdSet(Set<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Event> eventList = eventJpaRepository.findByIdIn(eventIds);

        if (eventList == null || eventList.isEmpty()) {
            return new HashSet<>();
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(eventList.stream().map(Event::getId).collect(Collectors.toList()));
        return eventList.stream()
                .map(e -> EventMapper.toFullDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toSet());
    }

    public Set<Event> getEventsByIds(Set<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Event> eventList = eventJpaRepository.findEventsWIthUsersByIdSet(eventIds);
        return new HashSet<>(eventList);
    }

    @Transactional
    private EventRequestStatusUpdateResult rejectRequests(Event event, List<ParticipationRequestDto> requests, EventRequestStatusUpdateRequest updateRequest) {
        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult();
        Map<Integer, ParticipationRequestDto> prDtoMap = requests.stream()
                .collect(Collectors.toMap(ParticipationRequestDto::getId, e -> e));
        for (int id : updateRequest.getRequestIds()) {
            ParticipationRequestDto prDto = prDtoMap.get(id);
            if (prDto == null) {
                throw new ElementNotFoundException("Запросу на обновление статуса, не найдено событие с id=" + id);
            }
            if (prDto.getStatus().equals(RequestStatus.PENDING.name())) {
                prDto.setStatus(RequestStatus.REJECTED.toString());
                updateResult.getRejectedRequests().add(prDto);
            } else {
                throw new CreateConditionException("Нельзя отклонить уже обработанную заявку id=" + id);
            }
        }
        participationService.updateAll(updateResult.getRejectedRequests(), event);
        return updateResult;
    }

    @Transactional
    private EventRequestStatusUpdateResult confirmAllRequests(Event event, List<ParticipationRequestDto> requests, EventRequestStatusUpdateRequest updateRequest) {
        int confirmedRequestsAmount = event.getConfirmedRequests();
        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult();
        Map<Integer, ParticipationRequestDto> prDtoMap = requests.stream()
                .collect(Collectors.toMap(ParticipationRequestDto::getId, e -> e));
        for (int id : updateRequest.getRequestIds()) {
            ParticipationRequestDto prDto = prDtoMap.get(id);
            if (prDto == null) {
                throw new ElementNotFoundException("Запросу на обновление статуса, не найдено событие с id=" + id);
            }
            if (prDto.getStatus().equals(RequestStatus.PENDING.name())) {
                prDto.setStatus(RequestStatus.CONFIRMED.toString());
                confirmedRequestsAmount++;
                event.setConfirmedRequests(confirmedRequestsAmount);
                eventJpaRepository.save(event);
            } else {
                throw new CreateConditionException("Нельзя подтвердить уже обработанную заявку id=" + id);
            }
        }
        participationService.updateAll(updateResult.getConfirmedRequests(), event);
        return updateResult;
    }

    @Transactional
    private EventRequestStatusUpdateResult confirmRequests(Event event, List<ParticipationRequestDto> requests, EventRequestStatusUpdateRequest updateRequest) {
        int confirmedRequestsAmount = event.getConfirmedRequests();
        int limit = event.getParticipantLimit();
        boolean limitAchieved = false;
        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult();
        Map<Integer, ParticipationRequestDto> prDtoMap = requests.stream()
                .collect(Collectors.toMap(ParticipationRequestDto::getId, e -> e));
        for (int id : updateRequest.getRequestIds()) {
            limitAchieved = confirmedRequestsAmount >= limit;
            ParticipationRequestDto prDto = prDtoMap.get(id);
            if (prDto == null) {
                throw new ElementNotFoundException("Запросу на обновление статуса, не найдено событие с id=" + id);
            }
            if (prDto.getStatus().equals(RequestStatus.PENDING.name())) {
                if (limitAchieved) {
                    prDto.setStatus(RequestStatus.REJECTED.toString());
                    participationService.update(prDto, event);
                    updateResult.getRejectedRequests().add(prDto);
                } else {
                    prDto.setStatus(RequestStatus.CONFIRMED.toString());
                    confirmedRequestsAmount++;
                    event.setConfirmedRequests(confirmedRequestsAmount);
                    eventJpaRepository.save(event);
                    updateResult.getConfirmedRequests().add(prDto);
                }
            } else {
                throw new CreateConditionException("Нельзя подтвердить уже обработанную заявку id=" + id);
            }
        }
        participationService.updateAll(updateResult.getRejectedRequests(), event);
        participationService.updateAll(updateResult.getConfirmedRequests(), event);
        if (limitAchieved) {
            throw new CreateConditionException("Превышен лимит на кол-во участников. Лимит = " + limit + ", кол-во подтвержденных заявок =" + confirmedRequestsAmount);
        }
        return updateResult;
    }
}