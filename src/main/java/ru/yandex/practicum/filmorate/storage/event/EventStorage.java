package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {

    Event add(Event event);

    List<Event> findByUserId(int userId);
}
