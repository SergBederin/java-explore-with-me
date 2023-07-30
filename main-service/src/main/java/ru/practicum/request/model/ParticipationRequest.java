package ru.practicum.request.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.events.model.Event;
import ru.practicum.users.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "requests")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDateTime created;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event eventsWithRequests;
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;
    @Enumerated(EnumType.STRING)
    private StatusEventRequestUpdateResult status;
}
