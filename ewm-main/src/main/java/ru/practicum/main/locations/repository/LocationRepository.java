package ru.practicum.main.locations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main.locations.model.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
}

