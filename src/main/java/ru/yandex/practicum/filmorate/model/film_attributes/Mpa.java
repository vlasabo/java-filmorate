package ru.yandex.practicum.filmorate.model.film_attributes;

public enum Mpa {
    G("G"),
    PG("PG"),
    PG13("PG-13"),
    R("R"),
    NC17("NC-17");

    private final String value;
    Mpa(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
