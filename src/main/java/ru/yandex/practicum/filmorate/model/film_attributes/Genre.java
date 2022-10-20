package ru.yandex.practicum.filmorate.model.film_attributes;

import lombok.Data;

@Data
public class Genre {
    private final String name;
    private final int id;

    public Genre(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
