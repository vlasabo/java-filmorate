package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @JsonProperty(value="eventId")
    private int id;

    @NonNull
    private Long timestamp;

    private EventType eventType;

    private OperationType operation;

    private int userId;

    private int entityId;
}
