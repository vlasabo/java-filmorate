package ru.yandex.practicum.filmorate.storage.film;


import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.film_attributes.Genre;
import ru.yandex.practicum.filmorate.model.film_attributes.Mpa;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film addFilm(Film film);

    List<Film> getAllFilms();


    Optional<Film> getFilmById(int filmId);

    Film updateFilm(Film film);

    List<Genre> getAllGenres();

    Genre getGenreById(int genreid);

    List<Mpa> getAllMpa();

    Mpa getMpaById(Integer mpaId);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> topNFilms(int count);
}
