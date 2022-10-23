package ru.yandex.practicum.filmorate.model.film_attributes;

import lombok.Data;

@Data
public class Mpa {
    private final String name;
    private final int id;

    public Mpa(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
