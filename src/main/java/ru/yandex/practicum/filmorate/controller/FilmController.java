package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.MyValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "films")
public class FilmController {
    private final HashMap<Integer, Film> allFilms = new HashMap<>();

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        int id = film.getId();

        if (allFilms.containsKey(id)) {
            allFilms.put(id, film);
            log.debug("correct update film {}", film);
        } else {
            log.debug("incorrect update film {}", film);
            throw new NotFoundException("no film with this id");
        }
        return film;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {

        film.setId(allFilms.size() + 1);
        allFilms.put(allFilms.size() + 1, film);
        log.debug("correct adding film {}", film);

        return film;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("get all films");
        return new ArrayList<>(allFilms.values());
    }


}

