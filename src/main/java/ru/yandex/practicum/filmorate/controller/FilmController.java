package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(value = "films")
@Slf4j
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService, UserService userService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.userService = userService;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmStorage.updateFilm(film);
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmStorage.addFilm(film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("get all films");
        return filmStorage.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable Integer filmId) {
        return filmService.getFilmById(filmId);
    }


    @PutMapping("{id}/like/{userId}")
    public Film likeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        Film film = filmService.getFilmById(id);
        User user = userService.getUserById(userId);
        filmService.addLike(film,user);
        log.debug("add like to film with id={} from user with id={}",id,userId);
        return film;
    }

}

