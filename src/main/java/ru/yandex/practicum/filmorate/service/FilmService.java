package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void addLike(Film film, User user){
        film.addLike(user.getId());
    }

    public void removeLike(Film film, User user){
        film.addLike(user.getId());
    }

    public List<Film> top10Films(){
       List<Film> allFilms = filmStorage.getAllFilms();
       return allFilms.stream().sorted(Comparator.comparing(Film::howManyLikes).reversed())
               .limit(10).collect(Collectors.toList());
    }

    public Film getFilmById(int filmId){
        Optional<Film> filmOptional = filmStorage.getAllFilms().stream().filter(f -> f.getId() == filmId).findFirst();
        if (filmOptional.isPresent()){
            return filmOptional.get();
        } else {
            log.debug("Film by id {} not found",filmId);
            throw new NotFoundException(String.format("Film by id %d not found",filmId));
        }
    }
}
