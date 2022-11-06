package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.practicum.filmorate.model.enums.EventType.*;
import static ru.yandex.practicum.filmorate.model.enums.OperationType.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventDbStorageTest {

    private final EventDbStorage eventStorage;
    private final UserDbStorage userStorage;

    @Test
    void findByUserId() {
        addUser(1);
        User user = addUser(2);

        int userId = user.getId();
        int entityId = 1;
        Event event = makeEvent(userId, entityId, LIKE, ADD);
        Event added = eventStorage.add(event);

        assertThat(added)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", LIKE)
                .hasFieldOrPropertyWithValue("operation", ADD);

        event = makeEvent(userId, entityId, LIKE, REMOVE);
        added = eventStorage.add(event);

        assertThat(added)
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", LIKE)
                .hasFieldOrPropertyWithValue("operation", REMOVE);

        entityId = 2;
        event = makeEvent(userId, entityId, REVIEW, ADD);
        added = eventStorage.add(event);

        assertThat(added)
                .hasFieldOrPropertyWithValue("id", 3)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", REVIEW)
                .hasFieldOrPropertyWithValue("operation", ADD);

        event = makeEvent(userId, entityId, REVIEW, UPDATE);
        added = eventStorage.add(event);

        assertThat(added)
                .hasFieldOrPropertyWithValue("id", 4)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", REVIEW)
                .hasFieldOrPropertyWithValue("operation", UPDATE);

        event = makeEvent(userId, entityId, REVIEW, REMOVE);
        added = eventStorage.add(event);

        assertThat(added)
                .hasFieldOrPropertyWithValue("id", 5)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", REVIEW)
                .hasFieldOrPropertyWithValue("operation", REMOVE);

        entityId = 3;
        event = makeEvent(userId, entityId, FRIEND, ADD);
        added = eventStorage.add(event);

        assertThat(added)
                .hasFieldOrPropertyWithValue("id", 6)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", FRIEND)
                .hasFieldOrPropertyWithValue("operation", ADD);

        event = makeEvent(userId, entityId, FRIEND, REMOVE);
        added = eventStorage.add(event);

        assertThat(added)
                .hasFieldOrPropertyWithValue("id", 7)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", FRIEND)
                .hasFieldOrPropertyWithValue("operation", REMOVE);


        // Проверка что реально в БД
        List<Event> events = eventStorage.findByUserId(userId);

        assertThat(events).hasSize(7);

        entityId = 1;
        Event event12 = events.get(1);
        assertThat(event12)
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", LIKE)
                .hasFieldOrPropertyWithValue("operation", REMOVE);

        entityId = 2;
        Event event14 = events.get(3);
        assertThat(event14)
                .hasFieldOrPropertyWithValue("id", 4)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("entityId", entityId)
                .hasFieldOrPropertyWithValue("eventType", REVIEW)
                .hasFieldOrPropertyWithValue("operation", UPDATE);
    }

    private Event makeEvent(int userId, int entityId, EventType eT, OperationType oT) {
        return new Event()
                .setId(0)
                .setTimestamp(LocalDateTime.now())
                .setEventType(eT)
                .setOperation(oT)
                .setUserId(userId)
                .setEntityId(entityId);
    }

    private User addUser(int i) {
        User user = new User(
                "test@test" + i + ".com",
                "login" + i,
                "vladimir" + i,
                LocalDate.ofEpochDay(1));
        userStorage.addUser(user);
        return user;
    }
}