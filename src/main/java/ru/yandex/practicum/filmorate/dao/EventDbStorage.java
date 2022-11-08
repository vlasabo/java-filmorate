package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Event add(Event event) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("events")
                .usingGeneratedKeyColumns("event_id");
        Map<String, Object> values = new HashMap<>();
        values.put("event_time", new Timestamp(event.getTimestamp()));
        values.put("event_type", event.getEventType().name());
        values.put("operation", event.getOperation().name());
        values.put("user_id", event.getUserId());
        values.put("entity_id", event.getEntityId());
        event.setId(simpleJdbcInsert.executeAndReturnKey(values).intValue());

        log.debug("New event added: {}", event);

        return event;
    }

    @Override
    public List<Event> findByUserId(int userId) {
        String sql = "select * from events where user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToEvent, userId);
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return new Event()
                .setId(rs.getInt("event_id"))
                .setTimestamp(rs.getTimestamp("event_time").getTime())
                .setEventType(EventType.valueOf(rs.getString("event_type")))
                .setOperation(OperationType.valueOf(rs.getString("operation")))
                .setUserId(rs.getInt("user_id"))
                .setEntityId(rs.getInt("entity_id"));
    }
}
