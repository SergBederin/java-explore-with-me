package ru.practicum.events.open;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.OpenEventRequests;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OpenEventsController {
    @Autowired
    private OpenEventsService service;

    @GetMapping(path = "/events")
    public List<EventShortDto> getEvents(@RequestParam(name = "text", required = false) String text,
                                         @RequestParam(name = "categories", required = false) List<Integer> categories,
                                         @RequestParam(name = "paid", required = false) String paid,
                                         @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                         @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                         @RequestParam(name = "onlyAvailable", required = false) String onlyAvailable,
                                         @RequestParam(name = "sort", required = false) String sort,
                                         @RequestParam(name = "from", defaultValue = "0") int from,
                                         @RequestParam(name = "size", defaultValue = "10") int size,
                                         HttpServletRequest request) {
        log.info("Выполняется запрос Get/events для получения событий с возможностью фильтрации.");
        return service.getEvents(OpenEventRequests.of(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size), request);
    }

    @GetMapping(path = "/events/{eventId}")
    public EventFullDto getEventsById(@PathVariable(name = "eventId") int eventId, HttpServletRequest request) {
        log.info("Выполняется запрос Get/events для получения подробной информации об опубликованном событии по id= {}.", eventId);

        return service.getEventsById(eventId, request);
    }
}
