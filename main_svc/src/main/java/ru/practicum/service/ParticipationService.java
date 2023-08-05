package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.dto.participationRequest.ParticipationMapper;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.CreateConditionException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.*;
import ru.practicum.repository.EventJpaRepository;
import ru.practicum.repository.ParticipationJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

    private final ParticipationJpaRepository participationJpaRepository;
    private final EventJpaRepository eventJpaRepository;
    private final UserService userService;

    @Transactional
    public ParticipationRequestDto create(int userId, EventFullDto eventFullDto) {
        ParticipationRequest newPartRequest = new ParticipationRequest();
        if (eventFullDto == null) {
            throw new BadParameterException("Пользователь не найден");
        }
        int eventId = eventFullDto.getId();

        User user = UserMapper.toUser(userService.getUserById(userId));

        newPartRequest.setRequester(user);
        newPartRequest.setEvent(EventMapper.toEvent(eventFullDto, user));
        newPartRequest.setCreated(LocalDateTime.now());

        ParticipationRequest duplicatedRequest = participationJpaRepository.getByUserIdAndEventId(userId, eventId);
        if (duplicatedRequest != null) {
            throw new CreateConditionException("Запрос от пользователя id=" + userId + " на событие c id=" + eventId + " уже существует");
        }

        if (eventFullDto.getInitiator().getId() == userId) {
            throw new CreateConditionException("Пользователь не может создавать запрос на участие в своем событии");
        }

        if (eventFullDto.getState() != EventState.PUBLISHED) {
            throw new CreateConditionException("Событие с id=" + eventId + " не опубликовано");
        }

        if (eventFullDto.getParticipantLimit() != 0) {
            if (eventFullDto.getConfirmedRequests() >= eventFullDto.getParticipantLimit()) {
                throw new CreateConditionException("У события с id=" + eventId + " достигнут лимит участников " + eventFullDto.getParticipantLimit());
            }
        }

        if ((eventFullDto.getParticipantLimit() == 0) || (!eventFullDto.isRequestModeration())) {
            newPartRequest.setStatus(RequestStatus.CONFIRMED);

            int confirmedRequestsAmount = eventFullDto.getConfirmedRequests();
            confirmedRequestsAmount++;
            eventFullDto.setConfirmedRequests(confirmedRequestsAmount);

            User eventInitiator = UserMapper.toUser(userService.getUserById(eventFullDto.getInitiator().getId()));
            eventJpaRepository.save(EventMapper.toEvent(eventFullDto, eventInitiator));
        }
        ParticipationRequest partRequest = participationJpaRepository.save(newPartRequest);
        return ParticipationMapper.toDto(partRequest);
    }

    public List<ParticipationRequestDto> getAllRequestsEventId(int eventId) {

        if (eventId < 0) {
            throw new BadParameterException("Id собтия должен быть больше 0");
        }

        List<ParticipationRequest> partRequests = participationJpaRepository.findAllByEventId(eventId);
        if (partRequests == null || partRequests.isEmpty()) {
            return new ArrayList<>();
        }

        return partRequests.stream()
                .map(ParticipationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(ParticipationRequestDto prDto, Event event) {
        User user = UserMapper.toUser(userService.getUserById(prDto.getRequester()));
        participationJpaRepository.save(ParticipationMapper.toPr(prDto, event, user));
    }

    @Transactional
    public void updateAll(List<ParticipationRequestDto> prDtoList, Event event) {

        List<Integer> userIds = prDtoList.stream()
                .map(ParticipationRequestDto::getRequester)
                .collect(Collectors.toList());
        Map<Integer, User> users = userService.getAllUsers(userIds).stream()
                .map(UserMapper::toUser)
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, ParticipationRequestDto> prDtoMap = prDtoList.stream()
                .collect(Collectors.toMap(ParticipationRequestDto::getId, e -> e));
        Map<Integer, User> requestUserMap = prDtoList.stream()
                .collect(Collectors.toMap(ParticipationRequestDto::getId, pr -> users.get(pr.getRequester())));

        List<ParticipationRequest> prList = prDtoList.stream()
                .map(pr -> ParticipationMapper.toPr(pr, event, requestUserMap.get(pr.getId())))
                .collect(Collectors.toList());

        participationJpaRepository.saveAll(prList);
    }

    public List<ParticipationRequestDto> getRequestsByUser(int userId) {

        UserDto userDto = userService.getUserById(userId);
        if (userDto == null) {
            throw new ElementNotFoundException("Пользователь с id= " + userId + " не найден");
        }

        List<ParticipationRequest> requestList = participationJpaRepository.findAllByUserId(userId);

        if (requestList == null) {
            return new ArrayList<>();
        }
        return requestList.stream()
                .map(ParticipationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto patchRequestCancel(int userId, int requestId) {

        UserDto userDto = userService.getUserById(userId);
        if (userDto == null) {
            throw new ElementNotFoundException("Пользователь с id= " + userId + " не найден");
        }

        ParticipationRequest partRequest = participationJpaRepository.findById(requestId)
                .orElseThrow(() -> new ElementNotFoundException("Заявка на участие с id= " + requestId + " не найден"));
        partRequest.setStatus(RequestStatus.CANCELED);
        ParticipationRequest partRequestUpdated = participationJpaRepository.save(partRequest);
        return ParticipationMapper.toDto(partRequestUpdated);
    }
}