package ru.practicum.locations.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.locations.model.Location;
import ru.practicum.locations.repository.LocationRepository;

@Service
@Transactional
@NoArgsConstructor
@Slf4j
public class LocationService {
    @Autowired
    private LocationRepository repository;

     public Location save(Location location) {
        return repository.save(location);
    }
}
