package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.main.stats.dto.ResponseDto;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {

    @Query("select new ru.practicum.main.stats.dto.ResponseDto(s.app, s.uri, count(s.app)) " +
            "from Stats as s " +
            "where s.uri like concat(?3, '%') " +
            "and s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "having count(s.ip) = 1" +
            "order by count(s.app) desc ")
    List<ResponseDto> findStatUriUnique(LocalDateTime start, LocalDateTime end, List<String> uri);

    @Query("select new ru.practicum.main.stats.dto.ResponseDto(s.app, s.uri, count(s.app))" +
            "from Stats as s " +
            "where s.uri like concat(?3, '%') " +
            "and s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "order by count(s.app) desc ")
    List<ResponseDto> findStatUri(LocalDateTime start, LocalDateTime end, List<String> uri);

    @Query("select new ru.practicum.main.stats.dto.ResponseDto(s.app, s.uri, count(s.app))" +
            "from Stats as s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "having count(s.ip) = 1 " +
            "order by count(s.app) desc ")
    List<ResponseDto> findStatUnique(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.main.stats.dto.ResponseDto(s.app, s.uri, count(s.app))" +
            "from Stats as s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "order by count(s.app) desc ")
    List<ResponseDto> findStat(LocalDateTime start, LocalDateTime end);
}