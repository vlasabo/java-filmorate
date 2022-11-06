package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.time.Instant;
import java.util.List;

import static ru.yandex.practicum.filmorate.model.enums.EventType.*;
import static ru.yandex.practicum.filmorate.model.enums.OperationType.*;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventStorage eventStorage;

    public List<Event> findByUserId(int userId) {
        return eventStorage.findByUserId(userId);
    }

    public Event addAddedLikeEvent(int userId, int entityId) {
        return eventStorage.add(makeEvent(userId, entityId, LIKE, ADD));
    }

    public Event addRemovedLikeEvent(int userId, int entityId) {
        return eventStorage.add(makeEvent(userId, entityId, LIKE, REMOVE));
    }

    public Event addAddedReviewEvent(int userId, int entityId) {
        return eventStorage.add(makeEvent(userId, entityId, REVIEW, ADD));
    }

    public Event addRemovedReviewEvent(int userId, int entityId) {
        return eventStorage.add(makeEvent(userId, entityId, REVIEW, REMOVE));
    }

    public Event addUpdatedReviewEvent(int userId, int entityId) {
        return eventStorage.add(makeEvent(userId, entityId, REVIEW, UPDATE));
    }

    public Event addAddedFriendEvent(int userId, int entityId) {
        return eventStorage.add(makeEvent(userId, entityId, FRIEND, ADD));
    }

    public Event addRemovedFriendEvent(int userId, int entityId) {
        return eventStorage.add(makeEvent(userId, entityId, FRIEND, REMOVE));
    }

    private Event makeEvent(int userId, int entityId, EventType eT, OperationType oT) {
        return new Event()
                .setId(0)
                .setTimestamp(Instant.now().toEpochMilli())
                .setEventType(eT)
                .setOperation(oT)
                .setUserId(userId)
                .setEntityId(entityId);
    }
}
