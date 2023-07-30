package ru.practicum.events.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class AdminEventRequests {
    private List<Integer> users;
    private List<State> states;
    private List<Integer> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static AdminEventRequests of(List<Integer> users,
                                        List<String> states,
                                        List<Integer> categories,
                                        String rangeStart,
                                        String rangeEnd,
                                        Integer from,
                                        Integer size) {
        AdminEventRequests request = new AdminEventRequests();
        if (rangeStart != null) {
            request.setRangeStart(LocalDateTime.parse(rangeStart, formatter));

        }
        if (rangeEnd != null) {
            request.setRangeEnd(LocalDateTime.parse(rangeEnd, formatter));
        }
        request.setFrom(from);
        request.setSize(size);

        if (users != null) {
            request.setUsers(users);
        }

        if (states != null) {
            List<State> stateList = new ArrayList<>();
            for (String state : states) {
                stateList.add(AdminEventRequests.State.valueOf(state.toUpperCase()));
            }
            request.setStates(stateList);
        }

        if (categories != null) {
            request.setCategories(categories);
        }

        return request;
    }

    public boolean hasUsers() {
        return users != null && !users.isEmpty();
    }

    public boolean hasStates() {
        return states != null && !states.isEmpty();
    }

    public boolean hasCategories() {
        return categories != null && !categories.isEmpty();
    }

    public enum State {
        PENDING,
        PUBLISHED,
        CANCELED,
        PUBLISH_EVENT,
        REJECT_EVENT,
        SEND_TO_REVIEW,
        CANCEL_REVIEW
    }
}