package ru.practicum.main.events.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.model.EventStatus;

import java.util.Optional;

@Repository
public interface EventsRepository extends JpaRepository<Event, Integer>, QuerydslPredicateExecutor<Event> {
    Page<Event> findEventsByInitiator_Id(int userId, Pageable page);

    Optional<Event> findEventByIdAndInitiator_Id(int eventId, int userId);

    Optional<Event> findEventsByIdAndStateIs(int eventId, EventStatus status);
}